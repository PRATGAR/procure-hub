package com.tradepost.procure.settlement.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.settlement.Authorization;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuthorizationRepository {

    private static final RowMapper<Authorization> MAPPER = (rs, n) -> new Authorization(
            rs.getLong("id"),
            rs.getLong("invoice_id"),
            rs.getString("auth_ref"),
            rs.getLong("amount_cents"),
            rs.getLong("captured_cents"),
            rs.getLong("refunded_cents"),
            rs.getString("status"),
            rs.getString("callback_url"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public AuthorizationRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insert(long invoiceId, String authRef, long amountCents, String callbackUrl) {
        Map<String, Object> row = new HashMap<>();
        row.put("invoice_id", invoiceId);
        row.put("auth_ref", authRef);
        row.put("amount_cents", amountCents);
        row.put("captured_cents", 0L);
        row.put("refunded_cents", 0L);
        row.put("status", "AUTHORIZED");
        row.put("callback_url", callbackUrl);
        return inserts.into("payment_authorizations", row);
    }

    public Authorization find(long id) {
        return jdbc.queryForObject(
                "SELECT id, invoice_id, auth_ref, amount_cents, captured_cents, refunded_cents, status, callback_url "
                        + "FROM payment_authorizations WHERE id = ?", MAPPER, id);
    }

    public void markCaptured(long id, long amountCents) {
        jdbc.update("UPDATE payment_authorizations SET captured_cents = captured_cents + ?, status = 'CAPTURED' WHERE id = ?",
                amountCents, id);
    }

    public void addRefund(long id, long amountCents) {
        String sql = "UPDATE payment_authorizations SET refunded_cents = refunded_cents + " + amountCents + " WHERE id = ?";
        jdbc.update(sql, id);
    }
}
