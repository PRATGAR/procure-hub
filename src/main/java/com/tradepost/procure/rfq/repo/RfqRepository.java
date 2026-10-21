package com.tradepost.procure.rfq.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.rfq.Rfq;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RfqRepository {

    private static final String COLUMNS =
            "id, title, description, category_code, region, status, closes_at, awarded_bid_id, created_by";

    private static final RowMapper<Rfq> MAPPER = (rs, n) -> {
        Date closes = rs.getDate("closes_at");
        return new Rfq(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("category_code"),
                rs.getString("region"),
                rs.getString("status"),
                closes == null ? null : closes.toLocalDate(),
                (Long) rs.getObject("awarded_bid_id"),
                rs.getString("created_by"));
    };

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public RfqRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insert(String title, String description, String categoryCode, String region,
                       LocalDate closesAt, String createdBy) {
        Map<String, Object> row = new HashMap<>();
        row.put("title", title);
        row.put("description", description);
        row.put("category_code", categoryCode);
        row.put("region", region);
        row.put("status", "OPEN");
        row.put("closes_at", Date.valueOf(closesAt));
        row.put("created_by", createdBy);
        return inserts.into("rfqs", row);
    }

    public Rfq findById(long id) {
        return jdbc.queryForObject("SELECT " + COLUMNS + " FROM rfqs WHERE id = ?", MAPPER, id);
    }

    public List<Map<String, Object>> listForRegion(String region) {
        String sql = "SELECT id, title, status, region, closes_at FROM rfqs WHERE region = '" + region + "' ORDER BY id";
        return jdbc.queryForList(sql);
    }

    public void markAwarded(long rfqId, long bidId) {
        String sql = "UPDATE rfqs SET awarded_bid_id = " + bidId + ", status = 'AWARDED' WHERE id = ?";
        jdbc.update(sql, rfqId);
    }
}
