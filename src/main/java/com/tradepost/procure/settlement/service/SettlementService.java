package com.tradepost.procure.settlement.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.FileStore;
import com.tradepost.procure.common.FxRates;
import com.tradepost.procure.common.Html;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.common.SafeJoin;
import com.tradepost.procure.config.TradepostProperties;
import com.tradepost.procure.settlement.Authorization;
import com.tradepost.procure.settlement.Invoice;
import com.tradepost.procure.settlement.repo.AuthorizationRepository;
import com.tradepost.procure.settlement.repo.InvoiceRepository;
import com.tradepost.procure.settlement.web.SettlementDtos.AuthorizeRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.CaptureRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.CreateInvoiceRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.CreditNoteRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.RefundRequest;
import com.tradepost.procure.supplier.service.SupplierService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final InvoiceRepository invoices;
    private final AuthorizationRepository authorizations;
    private final SupplierService suppliers;
    private final FxRates fx;
    private final FileStore store;
    private final OutboundHttp outbound;
    private final ApplicationEventPublisher events;
    private final AuditWriter audit;
    private final TradepostProperties properties;
    private final JdbcTemplate jdbc;

    public SettlementService(InvoiceRepository invoices, AuthorizationRepository authorizations,
                             SupplierService suppliers, FxRates fx, FileStore store, OutboundHttp outbound,
                             ApplicationEventPublisher events, AuditWriter audit, TradepostProperties properties,
                             JdbcTemplate jdbc) {
        this.invoices = invoices;
        this.authorizations = authorizations;
        this.suppliers = suppliers;
        this.fx = fx;
        this.store = store;
        this.outbound = outbound;
        this.events = events;
        this.audit = audit;
        this.properties = properties;
        this.jdbc = jdbc;
    }

    public long createInvoice(CreateInvoiceRequest request) {
        suppliers.get(request.supplierId());
        if (invoices.numberTaken(request.number())) {
            throw new ConflictException("the invoice number is already in use");
        }
        Invoice invoice = new Invoice(0, request.number(), request.supplierId(), request.orderId(),
                request.reference().trim(), request.currency().name(), request.totalCents(), 0, "OPEN");
        long id = invoices.insert(invoice);
        audit.append(Actors.current(), "invoice.created", request.number());
        return id;
    }

    public Map<String, Object> detail(long id) {
        Invoice invoice = invoices.find(id);
        List<Map<String, Object>> sameReference = invoices.sameReference(id, invoice.reference());
        long rate = fx.rateMicro(invoice.currency());
        long totalEur = invoice.totalCents() * rate / 1_000_000L;
        return Map.of("invoice", invoice, "sameReference", sameReference, "totalEurCents", totalEur);
    }

    public String printView(long id, String label) {
        Invoice invoice = invoices.find(id);
        String watermark = label == null || label.isBlank() ? "" : "<div class=\"watermark\">" + label + "</div>";
        return watermark + "<h1>Invoice " + Html.escape(invoice.number()) + "</h1>"
                + "<p>Reference: " + Html.escape(invoice.reference()) + "</p>"
                + "<p>Total: " + invoice.totalCents() / 100 + " " + Html.escape(invoice.currency()) + "</p>";
    }

    public String pdfText(long id, String template) throws IOException {
        Invoice invoice = invoices.find(id);
        Path file = SafeJoin.within(store.root().resolve(FileStore.PDF), template + ".txt");
        String body = Files.readString(file);
        return body.replace("{{number}}", invoice.number()).replace("{{reference}}", invoice.reference());
    }

    public String invoiceCsv() {
        StringBuilder csv = new StringBuilder("number,reference,currency,total_cents,paid_cents,status\n");
        for (Map<String, Object> row : invoices.all()) {
            csv.append(row.get("NUMBER")).append(',').append(row.get("REFERENCE")).append(',')
                    .append(row.get("CURRENCY")).append(',').append(row.get("TOTAL_CENTS")).append(',')
                    .append(row.get("PAID_CENTS")).append(',').append(row.get("STATUS")).append('\n');
        }
        return csv.toString();
    }

    public Map<String, Object> authorize(long invoiceId, AuthorizeRequest request) {
        Invoice invoice = invoices.find(invoiceId);
        long open = invoice.totalCents() - invoice.paidCents();
        if (request.amountCents() > open) {
            throw new InvalidRequestException("the amount exceeds the open balance");
        }
        String authRef = "AUTH-" + Long.toHexString(System.nanoTime());
        long id = authorizations.insert(invoiceId, authRef, request.amountCents(), request.callbackUrl());
        audit.append(Actors.current(), "payment.authorized", authRef);
        return Map.of("authorizationId", id, "authRef", authRef, "status", "AUTHORIZED");
    }

    public Map<String, Object> capture(long authorizationId, CaptureRequest request) {
        Authorization authorization = authorizations.find(authorizationId);
        if (!"AUTHORIZED".equals(authorization.status())) {
            throw new ConflictException("the authorization is not open for capture");
        }
        invoices.addPaid(authorization.invoiceId(), request.amountCents());
        authorizations.markCaptured(authorizationId, request.amountCents());
        Invoice invoice = invoices.find(authorization.invoiceId());
        invoices.updateStatus(invoice.id(), invoice.paidCents() >= invoice.totalCents() ? "PAID" : "PARTIALLY_PAID");
        events.publishEvent(new PaymentCaptured(authorization.invoiceId(), authorizationId, request.amountCents()));
        notifyCallback(authorization, request.amountCents());
        return Map.of("authorizationId", authorizationId, "capturedCents", request.amountCents(),
                "invoiceStatus", invoice.paidCents() >= invoice.totalCents() ? "PAID" : "PARTIALLY_PAID");
    }

    public Map<String, Object> refund(long authorizationId, RefundRequest request) {
        Authorization authorization = authorizations.find(authorizationId);
        if (!"CAPTURED".equals(authorization.status())) {
            throw new ConflictException("only captured authorizations can be refunded");
        }
        authorizations.addRefund(authorizationId, request.amountCents());
        invoices.subtractPaid(authorization.invoiceId(), request.amountCents());
        audit.append(Actors.current(), "payment.refunded", authorization.authRef());
        return Map.of("authorizationId", authorizationId, "refundedCents", request.amountCents());
    }

    public String checkoutTarget(long invoiceId) {
        Invoice invoice = invoices.find(invoiceId);
        return properties.checkoutUrl() + "?invoice=" + invoice.id();
    }

    public long createCreditNote(CreditNoteRequest request) {
        invoices.find(request.invoiceId());
        jdbc.update("INSERT INTO credit_notes (invoice_id, amount_cents, reason) VALUES (?, ?, ?)",
                request.invoiceId(), request.amountCents(), request.reason().trim());
        audit.append(Actors.current(), "creditnote.created", "invoice " + request.invoiceId());
        return request.invoiceId();
    }

    private void notifyCallback(Authorization authorization, long amountCents) {
        if (authorization.callbackUrl() == null || authorization.callbackUrl().isBlank()) {
            return;
        }
        try {
            outbound.deliver(authorization.callbackUrl(),
                    Map.of("authRef", authorization.authRef(), "capturedCents", amountCents));
        } catch (RestClientException e) {
            log.warn("capture callback for {} failed", authorization.authRef());
        }
    }
}
