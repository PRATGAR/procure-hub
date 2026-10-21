package com.tradepost.procure.report;

public enum SummaryDimension {
    SUPPLIER_STATUS("suppliers", "status"),
    SUPPLIER_COUNTRY("suppliers", "country"),
    ORDER_STATUS("purchase_orders", "status"),
    INVOICE_STATUS("invoices", "status"),
    SHIPMENT_CARRIER("shipments", "carrier");

    private final String table;
    private final String column;

    SummaryDimension(String table, String column) {
        this.table = table;
        this.column = column;
    }

    public String table() {
        return table;
    }

    public String column() {
        return column;
    }
}
