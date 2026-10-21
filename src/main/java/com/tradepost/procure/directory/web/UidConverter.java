package com.tradepost.procure.directory.web;

import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

public class UidConverter implements Converter<String, Uid> {

    private static final Pattern LOGIN = Pattern.compile("^[a-z0-9._-]{1,32}$");

    @Override
    public Uid convert(String source) {
        if (!LOGIN.matcher(source).matches()) {
            throw new IllegalArgumentException("malformed login name");
        }
        return new Uid(source);
    }
}
