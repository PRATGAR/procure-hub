package com.tradepost.procure.catalog;

public class ItemFacts {

    private final String sku;
    private final String name;
    private final String category;
    private final long priceCents;
    private final String currency;

    public ItemFacts(String sku, String name, String category, long priceCents, String currency) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.priceCents = priceCents;
        this.currency = currency;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public String getCurrency() {
        return currency;
    }
}
