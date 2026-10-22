package com.tradepost.procure.supplier.service;

import com.tradepost.procure.common.OutboundHttp;
import org.springframework.stereotype.Component;

@Component
public class WebsiteProbe {

    private final OutboundHttp outbound;

    public WebsiteProbe(OutboundHttp outbound) {
        this.outbound = outbound;
    }

    public int reach(String site) {
        String body = outbound.fetchText(site);
        return body == null ? -1 : body.length();
    }
}
