package com.tradepost.procure.supplier;

public record Contact(
        long id,
        long supplierId,
        String name,
        String email,
        String role,
        String note) {
}
