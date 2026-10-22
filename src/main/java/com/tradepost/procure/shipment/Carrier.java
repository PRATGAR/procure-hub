package com.tradepost.procure.shipment;

public enum Carrier {
    NORDEX("nordex", "NX"),
    CARGOLINE("cargoline", "CL"),
    BLUEPOST("bluepost", "BP");

    private final String key;
    private final String prefix;

    Carrier(String key, String prefix) {
        this.key = key;
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }

    public String key() {
        return key;
    }
}
