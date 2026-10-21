package com.tradepost.procure.integration.service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public record ImportSubmitted(String format, byte[] payload, AtomicReference<Map<String, Object>> result) {
}
