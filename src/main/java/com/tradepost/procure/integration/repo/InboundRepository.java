package com.tradepost.procure.integration.repo;

import com.tradepost.procure.common.Inserts;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InboundRepository {

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public InboundRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insert(String partnerCode, String eventType, String payloadHex, String signature) {
        Map<String, Object> row = new HashMap<>();
        row.put("partner_code", partnerCode);
        row.put("event_type", eventType);
        row.put("payload_hex", payloadHex);
        row.put("signature", signature);
        return inserts.into("inbound_events", row);
    }

    public String payloadHex(long id) {
        return jdbc.queryForObject("SELECT payload_hex FROM inbound_events WHERE id = ?", String.class, id);
    }
}
