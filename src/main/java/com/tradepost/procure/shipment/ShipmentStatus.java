package com.tradepost.procure.shipment;

import java.util.Locale;

public enum ShipmentStatus {
    CREATED,
    IN_TRANSIT,
    DELIVERED,
    EXCEPTION;

    public static ShipmentStatus fromCarrierText(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("deliver")) {
            return DELIVERED;
        }
        if (lower.contains("exception") || lower.contains("delay") || lower.contains("damage")) {
            return EXCEPTION;
        }
        return IN_TRANSIT;
    }
}
