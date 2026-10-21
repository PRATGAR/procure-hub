package com.tradepost.procure.settlement.service;

import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LedgerListener {

    private final List<LedgerWriter> writers;

    public LedgerListener(List<LedgerWriter> writers) {
        this.writers = writers;
    }

    @EventListener
    public void onPaymentCaptured(PaymentCaptured event) {
        for (LedgerWriter writer : writers) {
            writer.record(event);
        }
    }
}
