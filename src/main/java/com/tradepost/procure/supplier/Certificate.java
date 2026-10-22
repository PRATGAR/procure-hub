package com.tradepost.procure.supplier;

public record Certificate(long id, long supplierId, String issuer, String fileName) {
}
