package com.tradepost.procure.rfq.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.rfq.Bid;
import com.tradepost.procure.rfq.Question;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BidRepository {

    private static final String COLUMNS = "id, rfq_id, invitation_id, amount_cents, currency, incoterm, notes, status";

    private static final RowMapper<Bid> BID_MAPPER = (rs, n) -> new Bid(
            rs.getLong("id"),
            rs.getLong("rfq_id"),
            rs.getLong("invitation_id"),
            rs.getLong("amount_cents"),
            rs.getString("currency"),
            rs.getString("incoterm"),
            rs.getString("notes"),
            rs.getString("status"));

    private static final RowMapper<Question> QUESTION_MAPPER = (rs, n) -> new Question(
            rs.getLong("id"),
            rs.getLong("rfq_id"),
            rs.getLong("invitation_id"),
            rs.getString("question"),
            rs.getString("answer"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public BidRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insertBid(long rfqId, long invitationId, long amountCents, String currency, String incoterm, String notes) {
        Map<String, Object> row = new HashMap<>();
        row.put("rfq_id", rfqId);
        row.put("invitation_id", invitationId);
        row.put("amount_cents", amountCents);
        row.put("currency", currency);
        row.put("incoterm", incoterm);
        row.put("notes", notes);
        row.put("status", "SUBMITTED");
        return inserts.into("rfq_bids", row);
    }

    public Bid findForRfq(long rfqId, long bidId) {
        String sql = "SELECT " + COLUMNS + " FROM rfq_bids WHERE id = " + bidId + " AND rfq_id = " + rfqId;
        return jdbc.queryForObject(sql, BID_MAPPER);
    }

    public Bid findBound(long rfqId, long bidId) {
        return jdbc.queryForObject("SELECT " + COLUMNS + " FROM rfq_bids WHERE id = ? AND rfq_id = ?", BID_MAPPER, bidId, rfqId);
    }

    public long rateMicro(String currency) {
        String sql = "SELECT rate_micro FROM fx_rates WHERE currency = '" + currency + "'";
        Long rate = jdbc.queryForObject(sql, Long.class);
        return rate == null ? 1_000_000L : rate;
    }

    public int surchargeBp(String incoterm) {
        String sql = "SELECT surcharge_bp FROM freight_rules WHERE incoterm = '" + incoterm + "'";
        Integer surcharge = jdbc.queryForObject(sql, Integer.class);
        return surcharge == null ? 0 : surcharge;
    }

    public long insertQuestion(long rfqId, long invitationId, String question) {
        Map<String, Object> row = new HashMap<>();
        row.put("rfq_id", rfqId);
        row.put("invitation_id", invitationId);
        row.put("question", question);
        return inserts.into("rfq_questions", row);
    }

    public List<Question> questions(long rfqId) {
        return jdbc.query("SELECT id, rfq_id, invitation_id, question, answer FROM rfq_questions WHERE rfq_id = ? ORDER BY id",
                QUESTION_MAPPER, rfqId);
    }
}
