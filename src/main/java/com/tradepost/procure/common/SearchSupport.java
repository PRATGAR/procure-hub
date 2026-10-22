package com.tradepost.procure.common;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SearchSupport {

    private static final int CEILING = 200;

    private final JdbcTemplate jdbc;

    public SearchSupport(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> page(String select, String where, String orderBy, int limit, Object... args) {
        String sql = select + where + orderBy + " LIMIT " + QueryFragments.clampLimit(limit, CEILING);
        return jdbc.queryForList(sql, args);
    }
}
