package com.tradepost.procure.supplier.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BankChangeListener {

    private final SupplierAuditTrail trail;

    public BankChangeListener(SupplierAuditTrail trail) {
        this.trail = trail;
    }

    @EventListener
    public void onBankDetailsChanged(BankDetailsChanged event) {
        trail.bankDetailsChanged(event);
    }
}
