package com.tradepost.procure.pricing;

public record PriceRule(
        long id,
        String name,
        String scope,
        String discountExpr,
        String explanation,
        String currency,
        boolean active) {
}
