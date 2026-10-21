package com.tradepost.procure.order.service;

import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.config.TradepostProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("portal")
public class PortalChannel implements DispatchChannel {

    private final OutboundHttp outbound;
    private final TradepostProperties properties;

    public PortalChannel(OutboundHttp outbound, TradepostProperties properties) {
        this.outbound = outbound;
        this.properties = properties;
    }

    @Override
    public void dispatch(OrderApproved event) {
        String target = properties.dispatchUrls().get("portal");
        outbound.deliver(target, Map.of("poNumber", event.poNumber(), "supplierId", event.supplierId()));
    }
}
