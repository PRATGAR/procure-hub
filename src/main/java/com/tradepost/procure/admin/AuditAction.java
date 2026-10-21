package com.tradepost.procure.admin;

public enum AuditAction {
    SETTING("setting.updated"),
    ORDER("order.approved"),
    SUPPLIER("supplier.onboard"),
    CATALOG("catalog.import"),
    RFQ("rfq.created");

    private final String code;

    AuditAction(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
