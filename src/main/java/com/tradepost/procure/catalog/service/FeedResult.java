package com.tradepost.procure.catalog.service;

public class FeedResult {

    private int imported;

    public void add(int count) {
        this.imported += count;
    }

    public int imported() {
        return imported;
    }
}
