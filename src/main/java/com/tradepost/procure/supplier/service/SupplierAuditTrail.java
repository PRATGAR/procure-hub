package com.tradepost.procure.supplier.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SupplierAuditTrail {

    private static final Logger log = LoggerFactory.getLogger(SupplierAuditTrail.class);

    private final AuditWriter audit;

    public SupplierAuditTrail(AuditWriter audit) {
        this.audit = audit;
    }

    public void bankDetailsChanged(BankDetailsChanged event) {
        audit.append(Actors.current(), "supplier.bank.changed", "supplier " + event.supplierId());
        log.info("bank details changed for supplier " + event.supplierId() + " holder " + event.holder());
    }
}
