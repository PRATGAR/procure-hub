package com.tradepost.procure.settlement.web;

import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

public class WatermarkLabelConverter implements Converter<String, WatermarkLabel> {

    private static final Pattern SCRIPT_BLOCK = Pattern.compile("<script[^>]*>.*?</script>|<script[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public WatermarkLabel convert(String source) {
        String cleaned = SCRIPT_BLOCK.matcher(source).replaceAll("").trim();
        if (cleaned.length() > 120) {
            throw new IllegalArgumentException("label is too long");
        }
        return new WatermarkLabel(cleaned);
    }
}
