package com.tradepost.procure.order.service;

import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.config.TradepostProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("webhook")
public class WebhookChannel implements DispatchChannel {

    private final OutboundHttp outbound;
    private final TradepostProperties properties;

    public WebhookChannel(OutboundHttp outbound, TradepostProperties properties) {
        this.outbound = outbound;
        this.properties = properties;
    }

    @Override
    public void dispatch(OrderApproved event) {
        String target = properties.dispatchUrls().get("webhook");
        Map<String, Object> payload = Map.of("poNumber", event.poNumber(), "orderId", event.orderId(),
                "totalCents", event.totalCents());
        outbound.deliver(target, payload);
    }
}
