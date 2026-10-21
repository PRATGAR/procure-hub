package com.tradepost.procure.document.web;

import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

public class ArchiveNameConverter implements Converter<String, ArchiveName> {

    private static final Pattern ARCHIVE_FILE = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$");

    @Override
    public ArchiveName convert(String source) {
        if (!ARCHIVE_FILE.matcher(source).matches()) {
            throw new IllegalArgumentException("malformed archive name");
        }
        return new ArchiveName(source);
    }
}
