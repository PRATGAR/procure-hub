package com.tradepost.procure.order.service;

public record OrderApproved(long orderId, long supplierId, String poNumber, long totalCents) {
}
