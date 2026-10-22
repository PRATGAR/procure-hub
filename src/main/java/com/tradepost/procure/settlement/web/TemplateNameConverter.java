package com.tradepost.procure.settlement.web;

import java.nio.file.Path;
import org.springframework.core.convert.converter.Converter;

public class TemplateNameConverter implements Converter<String, TemplateName> {

    private static final Path TEMPLATE_AREA = Path.of("pdf");

    @Override
    public TemplateName convert(String source) {
        String name = source.trim();
        Path resolved = TEMPLATE_AREA.resolve(name).normalize();
        if (!resolved.toString().startsWith(TEMPLATE_AREA.toString())) {
            throw new IllegalArgumentException("unknown template");
        }
        return new TemplateName(name);
    }
}
