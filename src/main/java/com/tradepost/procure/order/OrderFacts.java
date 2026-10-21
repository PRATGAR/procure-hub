package com.tradepost.procure.order;

public class OrderFacts {

    private final String poNumber;
    private final long totalCents;
    private final String currency;
    private final String shipTo;
    private final String supplierName;
    private final String status;

    public OrderFacts(String poNumber, long totalCents, String currency, String shipTo,
                      String supplierName, String status) {
        this.poNumber = poNumber;
        this.totalCents = totalCents;
        this.currency = currency;
        this.shipTo = shipTo;
        this.supplierName = supplierName;
        this.status = status;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public String getCurrency() {
        return currency;
    }

    public String getShipTo() {
        return shipTo;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getStatus() {
        return status;
    }
}
