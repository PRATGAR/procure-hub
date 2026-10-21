package com.tradepost.procure.report.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class ScheduleListener {

    private static final Logger log = LoggerFactory.getLogger(ScheduleListener.class);

    private final Map<String, DeliveryChannel> channels;

    public ScheduleListener(Map<String, DeliveryChannel> channels) {
        this.channels = channels;
    }

    @EventListener
    public void onReportScheduled(ReportScheduled event) {
        boolean hasCallback = event.callbackUrl() != null && !event.callbackUrl().isBlank();
        DeliveryChannel channel = channels.get(hasCallback ? "reportWebhook" : "reportMail");
        try {
            channel.deliver(event);
        } catch (RestClientException e) {
            log.warn("first delivery for schedule {} failed", event.scheduleId());
        }
    }
}
