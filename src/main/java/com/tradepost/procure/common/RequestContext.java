package com.tradepost.procure.common;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public final class RequestContext {

    public static final String REGION = "tradepost.buyerRegion";
    public static final String UNIT = "tradepost.tenantUnit";

    private RequestContext() {
    }

    public static String region() {
        return read(REGION, "EU");
    }

    public static String unit() {
        return read(UNIT, "procurement");
    }

    private static String read(String name, String fallback) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return fallback;
        }
        Object value = attributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
        return value == null ? fallback : value.toString();
    }
}
