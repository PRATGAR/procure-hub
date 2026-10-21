package com.tradepost.procure.catalog;

public record Item(
        long id,
        String sku,
        String name,
        String description,
        String categoryCode,
        String tags,
        long unitPriceCents,
        String currency,
        Long supplierId,
        String status) {
}
