package com.tradepost.procure.integration;

public enum PartnerCode {
    ACMEFREIGHT("acmefreight"),
    NORDPARTS("nordparts"),
    HELVETIA("helvetia");

    private final String key;

    PartnerCode(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
