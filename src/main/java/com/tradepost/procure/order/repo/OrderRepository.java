package com.tradepost.procure.order.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.order.Order;
import com.tradepost.procure.order.OrderLine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private static final String COLUMNS = "id, po_number, supplier_id, status, total_cents, currency, instructions, "
            + "label_template, return_url, delivery_hook, approval_code, ship_to, created_by";

    private static final RowMapper<Order> MAPPER = (rs, n) -> new Order(
            rs.getLong("id"),
            rs.getString("po_number"),
            rs.getLong("supplier_id"),
            rs.getString("status"),
            rs.getLong("total_cents"),
            rs.getString("currency"),
            rs.getString("instructions"),
            rs.getString("label_template"),
            rs.getString("return_url"),
            rs.getString("delivery_hook"),
            rs.getString("approval_code"),
            rs.getString("ship_to"),
            rs.getString("created_by"));

    private static final RowMapper<OrderLine> LINE_MAPPER = (rs, n) -> new OrderLine(
            rs.getLong("id"),
            rs.getLong("order_id"),
            rs.getString("sku"),
            rs.getInt("qty"),
            rs.getInt("received_qty"),
            rs.getLong("unit_cents"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public OrderRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public int count() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM purchase_orders", Integer.class);
        return total == null ? 0 : total;
    }

    public long insert(Order order) {
        Map<String, Object> row = new HashMap<>();
        row.put("po_number", order.poNumber());
        row.put("supplier_id", order.supplierId());
        row.put("status", "DRAFT");
        row.put("total_cents", order.totalCents());
        row.put("currency", order.currency());
        row.put("instructions", order.instructions());
        row.put("label_template", order.labelTemplate());
        row.put("return_url", order.returnUrl());
        row.put("delivery_hook", order.deliveryHook());
        row.put("ship_to", order.shipTo());
        row.put("created_by", order.createdBy());
        return inserts.into("purchase_orders", row);
    }

    public void insertLine(long orderId, String sku, int qty, long unitCents) {
        jdbc.update("INSERT INTO purchase_order_lines (order_id, sku, qty, received_qty, unit_cents) VALUES (?, ?, ?, 0, ?)",
                orderId, sku, qty, unitCents);
    }

    public Order find(long id) {
        return jdbc.queryForObject("SELECT " + COLUMNS + " FROM purchase_orders WHERE id = ?", MAPPER, id);
    }

    public List<OrderLine> lines(long orderId) {
        return jdbc.query("SELECT id, order_id, sku, qty, received_qty, unit_cents FROM purchase_order_lines WHERE order_id = ? ORDER BY id",
                LINE_MAPPER, orderId);
    }

    public void updateStatus(long id, String status) {
        jdbc.update("UPDATE purchase_orders SET status = ? WHERE id = ?", status, id);
    }

    public void updateHook(long id, String hook) {
        jdbc.update("UPDATE purchase_orders SET delivery_hook = ? WHERE id = ?", hook, id);
    }

    public void updateReturnUrl(long id, String returnUrl) {
        jdbc.update("UPDATE purchase_orders SET return_url = ? WHERE id = ?", returnUrl, id);
    }

    public void markSubmitted(long id, String approvalCode) {
        jdbc.update("UPDATE purchase_orders SET status = 'SUBMITTED', approval_code = ? WHERE id = ?", approvalCode, id);
    }

    public int receive(long orderId, long lineId, int qty) {
        String sql = "UPDATE purchase_order_lines SET received_qty = received_qty + " + qty + " WHERE id = ? AND order_id = ?";
        return jdbc.update(sql, lineId, orderId);
    }
}
