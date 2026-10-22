package com.tradepost.procure.directory.service;

import com.tradepost.procure.common.OutboundHttp;
import org.springframework.stereotype.Component;

@Component
public class IdpMetadata {

    private final OutboundHttp outbound;

    public IdpMetadata(OutboundHttp outbound) {
        this.outbound = outbound;
    }

    public int fetch(String metadataUrl) {
        String document = outbound.fetchText(metadataUrl);
        return document == null ? 0 : document.length();
    }
}
