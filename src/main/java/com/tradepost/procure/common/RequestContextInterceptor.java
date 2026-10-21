package com.tradepost.procure.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequestContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String region = request.getHeader("X-Buyer-Region");
        if (region != null && !region.isBlank()) {
            request.setAttribute(RequestContext.REGION, region.trim());
        }
        String unit = request.getHeader("X-Tenant-Unit");
        if (unit != null && !unit.isBlank()) {
            request.setAttribute(RequestContext.UNIT, unit.trim());
        }
        return true;
    }
}
