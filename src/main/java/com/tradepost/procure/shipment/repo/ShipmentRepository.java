package com.tradepost.procure.shipment.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.shipment.Shipment;
import com.tradepost.procure.shipment.ShipmentStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ShipmentRepository {

    private static final String COLUMNS =
            "id, order_id, carrier, tracking_no, status, last_status, label_name, notes";

    private static final RowMapper<Shipment> MAPPER = (rs, n) -> new Shipment(
            rs.getLong("id"),
            rs.getLong("order_id"),
            rs.getString("carrier"),
            rs.getString("tracking_no"),
            rs.getString("status"),
            rs.getString("last_status"),
            rs.getString("label_name"),
            rs.getString("notes"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public ShipmentRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public boolean trackingTaken(String trackingNo) {
        String sql = "SELECT COUNT(*) FROM shipments WHERE tracking_no = '" + trackingNo + "'";
        Integer taken = jdbc.queryForObject(sql, Integer.class);
        return taken != null && taken > 0;
    }

    public long insert(long orderId, String carrier, String trackingNo, String notes) {
        Map<String, Object> row = new HashMap<>();
        row.put("order_id", orderId);
        row.put("carrier", carrier);
        row.put("tracking_no", trackingNo);
        row.put("status", ShipmentStatus.CREATED.name());
        row.put("notes", notes);
        return inserts.into("shipments", row);
    }

    public Shipment find(long id) {
        return jdbc.queryForObject("SELECT " + COLUMNS + " FROM shipments WHERE id = ?", MAPPER, id);
    }

    public Optional<Shipment> findByTracking(String carrier, String trackingNo) {
        return jdbc.query("SELECT " + COLUMNS + " FROM shipments WHERE carrier = ? AND tracking_no = ?",
                MAPPER, carrier, trackingNo).stream().findFirst();
    }

    public List<Map<String, Object>> peersByLastStatus(long id, String lastStatus) {
        String sql = "SELECT id, tracking_no, status FROM shipments WHERE last_status = '" + lastStatus + "' AND id <> " + id;
        return jdbc.queryForList(sql);
    }

    public int carrierLoad(String carrier) {
        String sql = "SELECT COUNT(*) FROM shipments WHERE carrier = '" + carrier + "'";
        Integer load = jdbc.queryForObject(sql, Integer.class);
        return load == null ? 0 : load;
    }

    public void updateLabel(long id, String labelName) {
        jdbc.update("UPDATE shipments SET label_name = ? WHERE id = ?", labelName, id);
    }

    public void updateLastStatus(long id, String lastStatus) {
        jdbc.update("UPDATE shipments SET last_status = ? WHERE id = ?", lastStatus, id);
    }

    public void updateStatus(long id, ShipmentStatus status) {
        String sql = "UPDATE shipments SET status = '" + status.name() + "' WHERE id = ?";
        jdbc.update(sql, id);
    }

    public void insertEvent(long shipmentId, String status, String detail) {
        jdbc.update("INSERT INTO shipment_events (shipment_id, status, detail) VALUES (?, ?, ?)", shipmentId, status, detail);
    }

    public List<Map<String, Object>> events(long shipmentId) {
        return jdbc.queryForList("SELECT status, detail, at_time FROM shipment_events WHERE shipment_id = ? ORDER BY id", shipmentId);
    }
}
