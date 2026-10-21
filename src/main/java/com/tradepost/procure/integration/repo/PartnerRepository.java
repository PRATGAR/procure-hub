package com.tradepost.procure.integration.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.integration.Partner;
import com.tradepost.procure.integration.Webhook;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PartnerRepository {

    private static final String COLUMNS = "id, code, name, base_url, portal_url, sig_alg, shared_secret, status";

    private static final RowMapper<Partner> MAPPER = (rs, n) -> new Partner(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("base_url"),
            rs.getString("portal_url"),
            rs.getString("sig_alg"),
            rs.getString("shared_secret"),
            rs.getString("status"));

    private static final RowMapper<Webhook> WEBHOOK_MAPPER = (rs, n) -> new Webhook(
            rs.getLong("id"),
            rs.getLong("partner_id"),
            rs.getString("url"),
            rs.getString("event_type"),
            rs.getString("secret"),
            rs.getString("status"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public PartnerRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insert(String code, String name, String baseUrl, String portalUrl, String sigAlg, String secret) {
        Map<String, Object> row = new HashMap<>();
        row.put("code", code);
        row.put("name", name);
        row.put("base_url", baseUrl);
        row.put("portal_url", portalUrl);
        row.put("sig_alg", sigAlg);
        row.put("shared_secret", secret);
        row.put("status", "ACTIVE");
        return inserts.into("partners", row);
    }

    public Partner find(long id) {
        return jdbc.queryForObject("SELECT " + COLUMNS + " FROM partners WHERE id = ?", MAPPER, id);
    }

    public Partner findByCode(String code) {
        return jdbc.queryForObject("SELECT " + COLUMNS + " FROM partners WHERE code = ?", MAPPER, code);
    }

    public boolean codeTaken(String code) {
        Integer taken = jdbc.queryForObject("SELECT COUNT(*) FROM partners WHERE code = ?", Integer.class, code);
        return taken != null && taken > 0;
    }

    public long insertWebhook(long partnerId, String url, String eventType, String secret) {
        Map<String, Object> row = new HashMap<>();
        row.put("partner_id", partnerId);
        row.put("url", url);
        row.put("event_type", eventType);
        row.put("secret", secret);
        row.put("status", "ACTIVE");
        return inserts.into("webhooks", row);
    }

    public Webhook findWebhook(long id) {
        return jdbc.queryForObject("SELECT id, partner_id, url, event_type, secret, status FROM webhooks WHERE id = ?",
                WEBHOOK_MAPPER, id);
    }

    public void recordDelivery(long webhookId, int statusCode, String snippet) {
        jdbc.update("INSERT INTO webhook_deliveries (webhook_id, status_code, snippet) VALUES (?, ?, ?)",
                webhookId, statusCode, snippet);
    }
}
