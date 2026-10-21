package com.tradepost.procure.order;

public enum OrderField {
    PO_NUMBER("poNumber"),
    TOTAL("totalCents"),
    CURRENCY("currency"),
    SHIP_TO("shipTo"),
    STATUS("status"),
    SUPPLIER("supplierName");

    private final String path;

    OrderField(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
