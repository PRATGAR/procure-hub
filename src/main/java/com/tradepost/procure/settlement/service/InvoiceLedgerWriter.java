package com.tradepost.procure.settlement.service;

import com.tradepost.procure.settlement.repo.LedgerRepository;
import org.springframework.stereotype.Component;

@Component
public class InvoiceLedgerWriter implements LedgerWriter {

    private final LedgerRepository ledger;

    public InvoiceLedgerWriter(LedgerRepository ledger) {
        this.ledger = ledger;
    }

    @Override
    public void record(PaymentCaptured event) {
        ledger.post(event.invoiceId(), event.amountCents(), "CAPTURE");
    }
}
