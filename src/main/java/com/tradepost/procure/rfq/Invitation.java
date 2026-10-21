package com.tradepost.procure.rfq;

public record Invitation(
        long id,
        long rfqId,
        long supplierId,
        String token,
        String accessPin,
        String status) {
}
