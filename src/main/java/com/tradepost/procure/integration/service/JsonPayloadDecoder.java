package com.tradepost.procure.integration.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("json")
public class JsonPayloadDecoder implements PayloadDecoder {

    @Override
    public Map<String, Object> decode(byte[] payload) {
        String text = new String(payload, StandardCharsets.UTF_8).trim();
        boolean object = text.startsWith("{") && text.endsWith("}");
        return Map.of("type", object ? "object" : "other", "length", text.length());
    }
}
