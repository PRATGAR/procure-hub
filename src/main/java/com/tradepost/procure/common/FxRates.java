package com.tradepost.procure.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FxRates {

    private final JdbcTemplate jdbc;

    public FxRates(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long rateMicro(String currency) {
        String sql = "SELECT rate_micro FROM fx_rates WHERE currency = '" + currency + "'";
        Long rate = jdbc.queryForObject(sql, Long.class);
        return rate == null ? 1_000_000L : rate;
    }
}
