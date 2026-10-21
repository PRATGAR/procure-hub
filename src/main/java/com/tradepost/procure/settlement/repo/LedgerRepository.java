package com.tradepost.procure.settlement.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LedgerRepository {

    private final JdbcTemplate jdbc;

    public LedgerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void post(long invoiceId, long amountCents, String kind) {
        String sql = "INSERT INTO ledger_entries (invoice_id, amount_cents, kind) VALUES ("
                + invoiceId + ", " + amountCents + ", '" + kind + "')";
        jdbc.update(sql);
    }
}
