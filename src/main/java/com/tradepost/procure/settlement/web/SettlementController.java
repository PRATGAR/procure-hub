package com.tradepost.procure.settlement.web;

import com.tradepost.procure.common.Downloads;
import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.common.Redirector;
import com.tradepost.procure.settlement.service.SettlementService;
import com.tradepost.procure.settlement.web.SettlementDtos.AuthorizationIssued;
import com.tradepost.procure.settlement.web.SettlementDtos.AuthorizeRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.CaptureRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.CaptureResult;
import com.tradepost.procure.settlement.web.SettlementDtos.CreateInvoiceRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.Created;
import com.tradepost.procure.settlement.web.SettlementDtos.CreditNoteCreated;
import com.tradepost.procure.settlement.web.SettlementDtos.CreditNoteRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.InvoiceDetail;
import com.tradepost.procure.settlement.web.SettlementDtos.InvoiceStyleSaved;
import com.tradepost.procure.settlement.web.SettlementDtos.PdfText;
import com.tradepost.procure.settlement.web.SettlementDtos.RefundRequest;
import com.tradepost.procure.settlement.web.SettlementDtos.RefundResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService service;
    private final PageWriter writer;
    private final Redirector redirector;

    public SettlementController(SettlementService service, PageWriter writer, Redirector redirector) {
        this.service = service;
        this.writer = writer;
        this.redirector = redirector;
    }

    @PostMapping("/invoices")
    public ResponseEntity<Created> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        long id = service.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(id, "OPEN"));
    }

    @GetMapping("/invoices/{id}")
    public InvoiceDetail invoice(@PathVariable long id) {
        return service.detail(id);
    }

    @PutMapping("/invoices/{id}/watermark")
    public InvoiceStyleSaved watermark(@PathVariable long id, @RequestParam WatermarkLabel label) {
        return service.setWatermark(id, label.value());
    }

    @GetMapping("/invoices/{id}/print")
    public void print(@PathVariable long id, HttpServletResponse response) throws IOException {
        writer.html(response, writer.page("Invoice", service.printView(id)));
    }

    @PutMapping("/invoices/{id}/template")
    public InvoiceStyleSaved template(@PathVariable long id, @RequestParam TemplateName template) {
        return service.setTemplate(id, template.value());
    }

    @GetMapping("/invoices/{id}/pdf")
    public PdfText pdf(@PathVariable long id) throws IOException {
        return new PdfText(id, service.pdfText(id));
    }

    @GetMapping("/invoices/export.csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        String csv = service.invoiceCsv();
        PrintWriter out = Downloads.csv(response, "invoices.csv");
        out.write(csv);
    }

    @PostMapping("/invoices/{id}/authorize")
    public ResponseEntity<AuthorizationIssued> authorize(@PathVariable long id,
                                                         @Valid @RequestBody AuthorizeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.authorize(id, request));
    }

    @PostMapping("/authorizations/{authId}/capture")
    public CaptureResult capture(@PathVariable long authId, @Valid @RequestBody CaptureRequest request) {
        return service.capture(authId, request);
    }

    @PostMapping("/authorizations/{authId}/refund")
    public RefundResult refund(@PathVariable long authId, @Valid @RequestBody RefundRequest request) {
        return service.refund(authId, request);
    }

    @GetMapping("/checkout/start")
    public void startCheckout(@RequestParam long invoiceId, HttpServletResponse response) throws IOException {
        redirector.to(response, service.checkoutTarget(invoiceId));
    }

    @PostMapping("/credit-notes")
    public ResponseEntity<CreditNoteCreated> creditNote(@Valid @RequestBody CreditNoteRequest request) {
        long invoiceId = service.createCreditNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreditNoteCreated(invoiceId));
    }
}
