package com.tradepost.procure.order.service;

import com.tradepost.procure.common.AuditWriter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SentNoticeListener {

    private final AuditWriter audit;

    public SentNoticeListener(AuditWriter audit) {
        this.audit = audit;
    }

    @EventListener
    public void onOrderSent(OrderSent event) {
        audit.append("system", "order.sent.noted", event.poNumber() + " notice " + event.serial());
    }
}
