package com.tradepost.procure.shipment;

public record Shipment(
        long id,
        long orderId,
        String carrier,
        String trackingNo,
        String status,
        String lastStatus,
        String labelName,
        String notes) {
}
