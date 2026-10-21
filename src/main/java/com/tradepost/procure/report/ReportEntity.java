package com.tradepost.procure.report;

public enum ReportEntity {
    SUPPLIER("suppliers"),
    ORDER("purchase_orders"),
    INVOICE("invoices");

    private final String table;

    ReportEntity(String table) {
        this.table = table;
    }

    public String table() {
        return table;
    }
}
