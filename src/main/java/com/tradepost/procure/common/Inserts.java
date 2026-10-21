package com.tradepost.procure.common;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

@Component
public class Inserts {

    private final JdbcTemplate jdbc;

    public Inserts(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long into(String table, Map<String, Object> values) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName(table)
                .usingGeneratedKeyColumns("id");
        return insert.executeAndReturnKey(values).longValue();
    }
}
