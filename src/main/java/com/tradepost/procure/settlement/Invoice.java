package com.tradepost.procure.settlement;

public record Invoice(
        long id,
        String number,
        long supplierId,
        Long orderId,
        String reference,
        String currency,
        long totalCents,
        long paidCents,
        String status) {
}
