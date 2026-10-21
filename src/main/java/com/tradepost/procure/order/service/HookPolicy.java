package com.tradepost.procure.order.service;

import com.tradepost.procure.common.HostAllowlist;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.config.TradepostProperties;
import org.springframework.stereotype.Component;

@Component
public class HookPolicy {

    private final TradepostProperties properties;

    public HookPolicy(TradepostProperties properties) {
        this.properties = properties;
    }

    public void require(String hook) {
        if (!HostAllowlist.allows(hook, properties.approvedCallbackHosts())) {
            throw new InvalidRequestException("delivery hook host is not approved");
        }
    }
}
