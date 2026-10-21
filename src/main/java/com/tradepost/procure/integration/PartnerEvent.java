package com.tradepost.procure.integration;

import java.io.Serializable;

public class PartnerEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String eventType;
    private final String reference;
    private final long amountCents;

    public PartnerEvent(String eventType, String reference, long amountCents) {
        this.eventType = eventType;
        this.reference = reference;
        this.amountCents = amountCents;
    }

    public String getEventType() {
        return eventType;
    }

    public String getReference() {
        return reference;
    }

    public long getAmountCents() {
        return amountCents;
    }

    @Override
    public String toString() {
        return eventType + ":" + reference + ":" + amountCents;
    }
}
