package com.tradepost.procure.settlement.web;

import com.tradepost.procure.common.CurrencyCode;
import com.tradepost.procure.settlement.Invoice;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public final class SettlementDtos {

    private SettlementDtos() {
    }

    public record CreateInvoiceRequest(
            @Min(1) long supplierId,
            Long orderId,
            @Min(1000) @Max(99999999) long number,
            @NotBlank @Size(max = 80) String reference,
            @NotNull CurrencyCode currency,
            @Min(1) long totalCents) {
    }

    public record AuthorizeRequest(
            @Min(1) long amountCents,
            @AllowedCallbackHost String callbackUrl) {
    }

    public record Created(long id, String status) {
    }

    public record InvoiceDetail(Invoice invoice, List<Map<String, Object>> sameReference, long totalEurCents) {
    }

    public record PdfText(long invoiceId, String text) {
    }

    public record AuthorizationIssued(long authorizationId, String authRef, String status) {
    }

    public record CaptureResult(long authorizationId, long capturedCents, String invoiceStatus) {
    }

    public record RefundResult(long authorizationId, long refundedCents) {
    }

    public record CreditNoteCreated(long invoiceId) {
    }

    public record InvoiceStyleSaved(long invoiceId, boolean saved) {
    }

    public record CaptureRequest(@Min(1) long amountCents) {
    }

    public record RefundRequest(@Min(1) long amountCents) {
    }

    public record CreditNoteRequest(
            @Min(1) long invoiceId,
            @Min(1) long amountCents,
            @NotBlank @Size(max = 300) String reason) {
    }
}
