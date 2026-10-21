package com.tradepost.procure.rfq.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.springframework.web.filter.OncePerRequestFilter;

public class PortalHeaderFilter extends OncePerRequestFilter {

    private static final int MAX_TOKEN_LENGTH = 64;
    private static final Pattern STATEMENT_SYNTAX = Pattern.compile("[\\s;]|--|/\\*");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader("X-Invite-Token");
        if (token != null && (token.length() > MAX_TOKEN_LENGTH || STATEMENT_SYNTAX.matcher(token).find())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        chain.doFilter(request, response);
    }
}
