package com.tradepost.procure.admin.web;

import java.time.LocalDate;
import org.springframework.core.convert.converter.Converter;

public class CutoffConverter implements Converter<String, Cutoff> {

    private static final int DATE_LENGTH = 10;

    @Override
    public Cutoff convert(String source) {
        String candidate = source.trim();
        if (candidate.length() < DATE_LENGTH) {
            throw new IllegalArgumentException("cutoff must start with a date");
        }
        LocalDate.parse(candidate.substring(0, DATE_LENGTH));
        return new Cutoff(candidate);
    }
}
