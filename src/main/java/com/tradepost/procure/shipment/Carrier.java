package com.tradepost.procure.shipment;

public enum Carrier {
    NORDEX("nordex"),
    CARGOLINE("cargoline"),
    BLUEPOST("bluepost");

    private final String key;

    Carrier(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
