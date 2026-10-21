package com.tradepost.procure.admin.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("purge")
public class PurgeTask implements MaintenanceTask {

    private final JdbcTemplate jdbc;

    public PurgeTask(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int run(String cutoff) {
        String sql = "DELETE FROM maintenance_runs WHERE ran_at < '" + cutoff + "'";
        return jdbc.update(sql);
    }
}
