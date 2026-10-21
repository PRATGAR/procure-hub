package com.tradepost.procure.pricing;

public record Quote(
        long id,
        String itemSku,
        int qty,
        String currency,
        long unitCents,
        long totalCents,
        Long ruleId) {
}
