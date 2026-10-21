package com.tradepost.procure.order;

public record OrderLine(long id, long orderId, String sku, int qty, int receivedQty, long unitCents) {
}
