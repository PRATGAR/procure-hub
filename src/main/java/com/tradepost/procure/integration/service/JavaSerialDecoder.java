package com.tradepost.procure.integration.service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("java")
public class JavaSerialDecoder implements PayloadDecoder {

    @Override
    public Map<String, Object> decode(byte[] payload) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(payload))) {
            Object decoded = in.readObject();
            return Map.of("type", decoded.getClass().getSimpleName(), "value", String.valueOf(decoded));
        }
    }
}
