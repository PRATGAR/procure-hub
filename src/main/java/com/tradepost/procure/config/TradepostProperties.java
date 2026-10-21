package com.tradepost.procure.config;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tradepost")
public record TradepostProperties(
        Storage storage,
        Directory directory,
        List<String> trustedReturnHosts,
        List<String> approvedCallbackHosts,
        List<String> partnerHosts,
        Map<String, String> carrierBaseUrls,
        Map<String, PartnerEndpoints> partnerEndpoints,
        Map<String, String> dispatchUrls,
        String checkoutUrl,
        String pspBaseUrl) {

    public record Storage(String root) {
    }

    public record Directory(String url, String baseDn) {
    }

    public record PartnerEndpoints(String events, String health, String catalog, String docs) {
    }
}
