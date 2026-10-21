package com.tradepost.procure.settlement.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.settlement.Invoice;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class InvoiceRepository {

    private static final String COLUMNS =
            "id, number, supplier_id, order_id, reference, currency, total_cents, paid_cents, status";

    private static final RowMapper<Invoice> MAPPER = (rs, n) -> new Invoice(
            rs.getLong("id"),
            rs.getString("number"),
            rs.getLong("supplier_id"),
            (Long) rs.getObject("order_id"),
            rs.getString("reference"),
            rs.getString("currency"),
            rs.getLong("total_cents"),
            rs.getLong("paid_cents"),
            rs.getString("status"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public InvoiceRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public boolean numberTaken(String number) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE number = '" + number + "'";
        Integer taken = jdbc.queryForObject(sql, Integer.class);
        return taken != null && taken > 0;
    }

    public long insert(Invoice invoice) {
        Map<String, Object> row = new HashMap<>();
        row.put("number", invoice.number());
        row.put("supplier_id", invoice.supplierId());
        row.put("order_id", invoice.orderId());
        row.put("reference", invoice.reference());
        row.put("currency", invoice.currency());
        row.put("total_cents", invoice.totalCents());
        row.put("paid_cents", 0L);
        row.put("status", "OPEN");
        return inserts.into("invoices", row);
    }

    public Invoice find(long id) {
        return jdbc.queryForObject("SELECT " + COLUMNS + " FROM invoices WHERE id = ?", MAPPER, id);
    }

    public List<Map<String, Object>> sameReference(long id, String reference) {
        String sql = "SELECT id, number, status FROM invoices WHERE reference = '" + reference + "' AND id <> " + id;
        return jdbc.queryForList(sql);
    }

    public void addPaid(long invoiceId, long amountCents) {
        String sql = "UPDATE invoices SET paid_cents = paid_cents + " + amountCents + " WHERE id = ?";
        jdbc.update(sql, invoiceId);
    }

    public void subtractPaid(long invoiceId, long amountCents) {
        jdbc.update("UPDATE invoices SET paid_cents = paid_cents - ? WHERE id = ?", amountCents, invoiceId);
    }

    public void updateStatus(long invoiceId, String status) {
        jdbc.update("UPDATE invoices SET status = ? WHERE id = ?", status, invoiceId);
    }

    public List<Map<String, Object>> all() {
        return jdbc.queryForList("SELECT number, reference, currency, total_cents, paid_cents, status FROM invoices ORDER BY id");
    }
}
