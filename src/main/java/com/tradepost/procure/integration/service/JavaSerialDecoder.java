package com.tradepost.procure.integration.service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.Map;

public class JavaSerialDecoder {

    private final ObjectInputFilter filter;

    public JavaSerialDecoder(ObjectInputFilter filter) {
        this.filter = filter;
    }

    public Map<String, Object> decode(byte[] payload) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(payload))) {
            in.setObjectInputFilter(filter);
            Object decoded = in.readObject();
            return Map.of("type", decoded.getClass().getSimpleName(), "value", String.valueOf(decoded));
        }
    }
}
