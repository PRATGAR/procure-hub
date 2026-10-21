package com.tradepost.procure.integration;

public record Webhook(long id, long partnerId, String url, String eventType, String secret, String status) {
}
