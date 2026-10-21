package com.tradepost.procure.rfq;

public record Question(long id, long rfqId, long invitationId, String question, String answer) {
}
