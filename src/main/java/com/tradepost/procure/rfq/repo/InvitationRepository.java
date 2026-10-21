package com.tradepost.procure.rfq.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.rfq.Invitation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class InvitationRepository {

    private static final String COLUMNS = "id, rfq_id, supplier_id, token, access_pin, status";

    private static final RowMapper<Invitation> MAPPER = (rs, n) -> new Invitation(
            rs.getLong("id"),
            rs.getLong("rfq_id"),
            rs.getLong("supplier_id"),
            rs.getString("token"),
            rs.getString("access_pin"),
            rs.getString("status"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public InvitationRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insert(long rfqId, long supplierId, String token, String pin) {
        Map<String, Object> row = new HashMap<>();
        row.put("rfq_id", rfqId);
        row.put("supplier_id", supplierId);
        row.put("token", token);
        row.put("access_pin", pin);
        row.put("status", "SENT");
        return inserts.into("rfq_invitations", row);
    }

    public Invitation findByToken(String token) {
        String sql = "SELECT " + COLUMNS + " FROM rfq_invitations WHERE token = '" + token + "'";
        return jdbc.queryForObject(sql, MAPPER);
    }

    public List<Invitation> findOpenByToken(String token) {
        return jdbc.query("SELECT " + COLUMNS + " FROM rfq_invitations WHERE token = ? AND status IN ('SENT', 'VIEWED')",
                MAPPER, token);
    }

    public void updateStatus(long id, String status) {
        jdbc.update("UPDATE rfq_invitations SET status = ? WHERE id = ?", status, id);
    }
}
