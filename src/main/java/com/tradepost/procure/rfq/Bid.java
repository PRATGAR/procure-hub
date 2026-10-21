package com.tradepost.procure.rfq;

public record Bid(
        long id,
        long rfqId,
        long invitationId,
        long amountCents,
        String currency,
        String incoterm,
        String notes,
        String status) {
}
