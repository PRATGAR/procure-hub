package com.tradepost.procure.integration;

public record Partner(
        long id,
        String code,
        String name,
        String baseUrl,
        String portalUrl,
        String sigAlg,
        String sharedSecret,
        String status) {
}
