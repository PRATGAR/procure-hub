package com.tradepost.procure.report.service;

import com.tradepost.procure.common.OutboundHttp;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("reportWebhook")
public class WebhookDelivery implements DeliveryChannel {

    private final OutboundHttp outbound;

    public WebhookDelivery(OutboundHttp outbound) {
        this.outbound = outbound;
    }

    @Override
    public void deliver(ReportScheduled event) {
        outbound.deliver(event.callbackUrl(), Map.of("reportId", event.reportId(), "scheduleId", event.scheduleId()));
    }
}
