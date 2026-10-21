package com.tradepost.procure.shipment;

public enum ShipmentSort {
    CREATED("id"),
    CARRIER("carrier"),
    STATUS("status"),
    TRACKING("tracking_no");

    private final String column;

    ShipmentSort(String column) {
        this.column = column;
    }

    public String column() {
        return column;
    }

    public static ShipmentSort parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return CREATED;
        }
        for (ShipmentSort sort : values()) {
            if (sort.name().equalsIgnoreCase(raw.trim())) {
                return sort;
            }
        }
        return CREATED;
    }
}
