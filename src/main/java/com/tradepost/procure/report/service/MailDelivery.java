package com.tradepost.procure.report.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("reportMail")
public class MailDelivery implements DeliveryChannel {

    private static final Logger log = LoggerFactory.getLogger(MailDelivery.class);

    @Override
    public void deliver(ReportScheduled event) {
        log.info("report {} queued for mail delivery (schedule {})", event.reportId(), event.scheduleId());
    }
}
