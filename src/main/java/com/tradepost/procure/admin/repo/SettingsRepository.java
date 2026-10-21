package com.tradepost.procure.admin.repo;

import com.tradepost.procure.common.Inserts;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SettingsRepository {

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public SettingsRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public void upsert(String key, String value, String changedBy) {
        int touched = jdbc.update("UPDATE setting_entries SET setting_value = ?, changed_by = ? WHERE setting_key = ?",
                value, changedBy, key);
        if (touched == 0) {
            jdbc.update("INSERT INTO setting_entries (setting_key, setting_value, changed_by) VALUES (?, ?, ?)",
                    key, value, changedBy);
        }
    }

    public Optional<String> find(String key) {
        List<String> found = jdbc.queryForList("SELECT setting_value FROM setting_entries WHERE setting_key = ?", String.class, key);
        return found.stream().findFirst();
    }

    public long insertBundle(String name, String blobHex) {
        Map<String, Object> row = new HashMap<>();
        row.put("name", name);
        row.put("blob_hex", blobHex);
        return inserts.into("settings_bundles", row);
    }

    public String bundleHex(long id) {
        return jdbc.queryForObject("SELECT blob_hex FROM settings_bundles WHERE id = ?", String.class, id);
    }

    public void recordRun(String task, String cutoff, int deleted) {
        jdbc.update("INSERT INTO maintenance_runs (task, cutoff, deleted) VALUES (?, ?, ?)", task, cutoff, deleted);
    }
}
