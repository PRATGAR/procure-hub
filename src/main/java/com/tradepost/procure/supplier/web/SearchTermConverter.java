package com.tradepost.procure.supplier.web;

import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

public class SearchTermConverter implements Converter<String, SearchTerm> {

    private static final Pattern NAME_CHARS = Pattern.compile("^[\\p{L}\\p{N} .,'&()%_-]{0,80}$");

    @Override
    public SearchTerm convert(String source) {
        if (!NAME_CHARS.matcher(source).matches()) {
            throw new IllegalArgumentException("malformed search term");
        }
        return new SearchTerm(source.trim());
    }
}
