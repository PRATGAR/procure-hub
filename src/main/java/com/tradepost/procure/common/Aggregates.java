package com.tradepost.procure.common;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class Aggregates {

    private final JdbcTemplate jdbc;

    public Aggregates(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> countBy(String table, String column) {
        String sql = "SELECT " + column + " AS bucket, COUNT(*) AS total FROM " + table + " GROUP BY " + column;
        return jdbc.queryForList(sql);
    }
}
