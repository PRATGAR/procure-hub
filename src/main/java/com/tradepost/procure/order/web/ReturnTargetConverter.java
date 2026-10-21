package com.tradepost.procure.order.web;

import java.util.List;
import org.springframework.core.convert.converter.Converter;

public class ReturnTargetConverter implements Converter<String, ReturnTarget> {

    private final List<String> trustedHosts;

    public ReturnTargetConverter(List<String> trustedHosts) {
        this.trustedHosts = trustedHosts;
    }

    @Override
    public ReturnTarget convert(String source) {
        String target = source.trim();
        if (target.startsWith("/") && !target.startsWith("//")) {
            return new ReturnTarget(target);
        }
        for (String host : trustedHosts) {
            if (target.startsWith("https://" + host)) {
                return new ReturnTarget(target);
            }
        }
        throw new IllegalArgumentException("return target is not accepted");
    }
}
