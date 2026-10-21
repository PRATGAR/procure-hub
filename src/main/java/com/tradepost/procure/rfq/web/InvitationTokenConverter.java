package com.tradepost.procure.rfq.web;

import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

public class InvitationTokenConverter implements Converter<String, InvitationToken> {

    private static final Pattern HEX = Pattern.compile("^[a-f0-9]{16,48}$");

    @Override
    public InvitationToken convert(String source) {
        if (!HEX.matcher(source).matches()) {
            throw new IllegalArgumentException("malformed invitation token");
        }
        return new InvitationToken(source);
    }
}
