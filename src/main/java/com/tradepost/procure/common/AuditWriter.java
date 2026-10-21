package com.tradepost.procure.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditWriter {

    private final JdbcTemplate jdbc;

    public AuditWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void append(String actor, String action, String detail) {
        jdbc.update("INSERT INTO audit_log (actor, action, detail) VALUES (?, ?, ?)", actor, action, detail);
    }
}
