package com.tradepost.procure.admin.web;

import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

public class StatColumnConverter implements Converter<String, StatColumn> {

    private static final Pattern COLUMN_EXPRESSION = Pattern.compile("^[A-Za-z_][A-Za-z0-9_(),. ]{0,119}$");

    @Override
    public StatColumn convert(String source) {
        String expression = source.trim();
        if (!COLUMN_EXPRESSION.matcher(expression).matches()) {
            throw new IllegalArgumentException("malformed column expression");
        }
        return new StatColumn(expression);
    }
}
