package com.tradepost.procure.pricing;

public class QuoteFacts {

    private final String sku;
    private final int qty;
    private final long unitCents;
    private final long totalCents;
    private final String currency;

    public QuoteFacts(String sku, int qty, long unitCents, long totalCents, String currency) {
        this.sku = sku;
        this.qty = qty;
        this.unitCents = unitCents;
        this.totalCents = totalCents;
        this.currency = currency;
    }

    public String getSku() {
        return sku;
    }

    public int getQty() {
        return qty;
    }

    public long getUnitCents() {
        return unitCents;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public String getCurrency() {
        return currency;
    }
}
