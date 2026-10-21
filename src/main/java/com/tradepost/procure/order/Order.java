package com.tradepost.procure.order;

public record Order(
        long id,
        String poNumber,
        long supplierId,
        String status,
        long totalCents,
        String currency,
        String instructions,
        String labelTemplate,
        String returnUrl,
        String deliveryHook,
        String approvalCode,
        String shipTo,
        String createdBy) {
}
