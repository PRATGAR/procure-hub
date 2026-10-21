package com.tradepost.procure.settlement;

public record Authorization(
        long id,
        long invoiceId,
        String authRef,
        long amountCents,
        long capturedCents,
        long refundedCents,
        String status,
        String callbackUrl) {
}
