package com.tradepost.procure.settlement.service;

public interface LedgerWriter {

    void record(PaymentCaptured event);
}
