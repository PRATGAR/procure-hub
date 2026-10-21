package com.tradepost.procure.supplier.service;

public record BankDetailsChanged(long supplierId, String holder, String bic) {
}
