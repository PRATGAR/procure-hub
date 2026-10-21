package com.tradepost.procure.settlement.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import org.springframework.stereotype.Component;

@Component
public class AuditLedgerWriter implements LedgerWriter {

    private final AuditWriter audit;

    public AuditLedgerWriter(AuditWriter audit) {
        this.audit = audit;
    }

    @Override
    public void record(PaymentCaptured event) {
        audit.append(Actors.current(), "settlement.captured",
                "invoice " + event.invoiceId() + " amount " + event.amountCents());
    }
}
