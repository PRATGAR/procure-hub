package com.tradepost.procure.shipment.web;

import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

public class ExportNameConverter implements Converter<String, ExportName> {

    private static final Pattern FILE_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$");

    @Override
    public ExportName convert(String source) {
        if (!FILE_NAME.matcher(source).matches()) {
            throw new IllegalArgumentException("malformed export name");
        }
        return new ExportName(source);
    }
}
