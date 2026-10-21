package com.tradepost.procure.supplier;

public record Supplier(
        long id,
        String legalName,
        String displayName,
        String country,
        String taxId,
        String status,
        String website,
        String description) {
}
