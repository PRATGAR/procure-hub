package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.NotFoundException;
import com.tradepost.procure.config.TradepostProperties;
import com.tradepost.procure.config.TradepostProperties.PartnerEndpoints;
import com.tradepost.procure.integration.PartnerCode;
import org.springframework.stereotype.Component;

@Component
public class PartnerUrls {

    private final TradepostProperties properties;

    public PartnerUrls(TradepostProperties properties) {
        this.properties = properties;
    }

    public String events(PartnerCode code) {
        return endpoints(code).events();
    }

    public String health(PartnerCode code) {
        return endpoints(code).health();
    }

    public String catalog(PartnerCode code) {
        return endpoints(code).catalog();
    }

    public String docs(PartnerCode code) {
        return endpoints(code).docs();
    }

    private PartnerEndpoints endpoints(PartnerCode code) {
        PartnerEndpoints endpoints = properties.partnerEndpoints().get(code.key());
        if (endpoints == null) {
            throw new NotFoundException("partner endpoints", code);
        }
        return endpoints;
    }
}
