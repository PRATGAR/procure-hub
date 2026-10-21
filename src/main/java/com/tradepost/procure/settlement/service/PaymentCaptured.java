package com.tradepost.procure.settlement.service;

public record PaymentCaptured(long invoiceId, long authorizationId, long amountCents) {
}
