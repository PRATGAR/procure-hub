package com.tradepost.procure.common;

import org.springframework.core.convert.converter.Converter;

public class NumericIdConverter implements Converter<String, NumericId> {

    @Override
    public NumericId convert(String source) {
        long parsed = Long.parseLong(source.trim());
        return new NumericId(Long.toString(parsed));
    }
}
