package com.tradepost.procure.settlement.web;

import com.tradepost.procure.common.CurrencyCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class SettlementDtos {

    private SettlementDtos() {
    }

    public record CreateInvoiceRequest(
            @Min(1) long supplierId,
            Long orderId,
            @NotBlank @Pattern(regexp = "^INV-[0-9]{4,8}$") String number,
            @NotBlank @Size(max = 80) String reference,
            @NotNull CurrencyCode currency,
            @Min(1) long totalCents) {
    }

    public record AuthorizeRequest(
            @Min(1) long amountCents,
            @AllowedCallbackHost String callbackUrl) {
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
