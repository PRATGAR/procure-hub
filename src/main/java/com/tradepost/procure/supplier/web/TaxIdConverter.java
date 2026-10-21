package com.tradepost.procure.supplier.web;

import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

public class TaxIdConverter implements Converter<String, TaxId> {

    private static final Pattern REGISTRY_ID = Pattern.compile("^[A-Z0-9]{8,14}");

    @Override
    public TaxId convert(String source) {
        String candidate = source.trim();
        if (!REGISTRY_ID.matcher(candidate).find()) {
            throw new IllegalArgumentException("malformed tax id");
        }
        return new TaxId(candidate);
    }
}
