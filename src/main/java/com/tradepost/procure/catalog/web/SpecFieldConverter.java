package com.tradepost.procure.catalog.web;

import java.util.List;
import org.springframework.core.convert.converter.Converter;

public class SpecFieldConverter implements Converter<String, SpecField> {

    private static final List<String> FIELDS = List.of("sku", "name", "category", "currency", "status", "tags");

    @Override
    public SpecField convert(String source) {
        String key = source.trim();
        if (FIELDS.stream().noneMatch(key::startsWith)) {
            throw new IllegalArgumentException("unknown specification field");
        }
        return new SpecField(key);
    }
}
