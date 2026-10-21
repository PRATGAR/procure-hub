package com.tradepost.procure.pricing.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.pricing.PriceRule;
import com.tradepost.procure.pricing.Quote;
import com.tradepost.procure.pricing.RuleScope;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PricingRepository {

    private static final String RULE_COLUMNS = "id, name, scope, discount_expr, explanation, currency, active";

    private static final RowMapper<PriceRule> RULE_MAPPER = (rs, n) -> new PriceRule(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("scope"),
            rs.getString("discount_expr"),
            rs.getString("explanation"),
            rs.getString("currency"),
            rs.getBoolean("active"));

    private static final RowMapper<Quote> QUOTE_MAPPER = (rs, n) -> new Quote(
            rs.getLong("id"),
            rs.getString("item_sku"),
            rs.getInt("qty"),
            rs.getString("currency"),
            rs.getLong("unit_cents"),
            rs.getLong("total_cents"),
            (Long) rs.getObject("rule_id"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public PricingRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insertRule(String name, String scope, String discountExpr, String explanation, String currency) {
        Map<String, Object> row = new HashMap<>();
        row.put("name", name);
        row.put("scope", scope);
        row.put("discount_expr", discountExpr);
        row.put("explanation", explanation);
        row.put("currency", currency);
        row.put("active", Boolean.TRUE);
        return inserts.into("price_rules", row);
    }

    public List<Map<String, Object>> rulesByScope(RuleScope scope) {
        String sql = "SELECT " + RULE_COLUMNS + " FROM price_rules WHERE scope = '" + scope.name() + "' ORDER BY id";
        return jdbc.queryForList(sql);
    }

    public List<Map<String, Object>> allRules() {
        return jdbc.queryForList("SELECT " + RULE_COLUMNS + " FROM price_rules ORDER BY id");
    }

    public List<PriceRule> activeRules(RuleScope scope) {
        return jdbc.query("SELECT " + RULE_COLUMNS + " FROM price_rules WHERE active = TRUE AND scope = ? ORDER BY id",
                RULE_MAPPER, scope.name());
    }

    public PriceRule findRule(long id) {
        return jdbc.queryForObject("SELECT " + RULE_COLUMNS + " FROM price_rules WHERE id = ?", RULE_MAPPER, id);
    }

    public long unitPrice(String sku) {
        Long price = jdbc.queryForObject("SELECT unit_price_cents FROM catalog_items WHERE sku = ?", Long.class, sku);
        return price == null ? 0L : price;
    }

    public int tierBp(int qty) {
        String sql = "SELECT discount_bp FROM price_tiers WHERE min_qty <= " + qty + " ORDER BY min_qty DESC LIMIT 1";
        List<Integer> tiers = jdbc.queryForList(sql, Integer.class);
        return tiers.isEmpty() ? 0 : tiers.get(0);
    }

    public int tierBpFor(int qty) {
        List<Integer> tiers = jdbc.queryForList(
                "SELECT discount_bp FROM price_tiers WHERE min_qty <= ? ORDER BY min_qty DESC LIMIT 1", Integer.class, qty);
        return tiers.isEmpty() ? 0 : tiers.get(0);
    }

    public int volumeBp(int units) {
        String sql = "SELECT COALESCE(MAX(discount_bp), 0) FROM price_tiers WHERE min_qty <= " + units;
        Integer volume = jdbc.queryForObject(sql, Integer.class);
        return volume == null ? 0 : volume;
    }

    public long insertQuote(String sku, int qty, String currency, long unitCents, long totalCents, Long ruleId) {
        Map<String, Object> row = new HashMap<>();
        row.put("item_sku", sku);
        row.put("qty", qty);
        row.put("currency", currency);
        row.put("unit_cents", unitCents);
        row.put("total_cents", totalCents);
        row.put("rule_id", ruleId);
        return inserts.into("price_quotes", row);
    }

    public Quote findQuote(long id) {
        return jdbc.queryForObject(
                "SELECT id, item_sku, qty, currency, unit_cents, total_cents, rule_id FROM price_quotes WHERE id = ?",
                QUOTE_MAPPER, id);
    }
}
