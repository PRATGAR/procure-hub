package com.tradepost.procure.supplier.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.supplier.Supplier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SupplierRepository {

    private static final String COLUMNS =
            "id, legal_name, display_name, country, tax_id, status, website, description";

    private static final RowMapper<Supplier> MAPPER = (rs, n) -> new Supplier(
            rs.getLong("id"),
            rs.getString("legal_name"),
            rs.getString("display_name"),
            rs.getString("country"),
            rs.getString("tax_id"),
            rs.getString("status"),
            rs.getString("website"),
            rs.getString("description"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public SupplierRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public List<Supplier> search(String q) {
        String sql = "SELECT " + COLUMNS + " FROM suppliers WHERE legal_name LIKE '%" + q + "%' ORDER BY legal_name";
        return jdbc.query(sql, MAPPER);
    }

    public Supplier findById(long id) {
        String sql = "SELECT " + COLUMNS + " FROM suppliers WHERE id = " + id;
        return jdbc.queryForObject(sql, MAPPER);
    }

    public boolean existsByTaxId(String taxId) {
        String sql = "SELECT COUNT(*) FROM suppliers WHERE tax_id = '" + taxId + "'";
        Integer count = jdbc.queryForObject(sql, Integer.class);
        return count != null && count > 0;
    }

    public long insert(String legalName, String country, String taxId, String website, String description) {
        Map<String, Object> row = new HashMap<>();
        row.put("legal_name", legalName);
        row.put("display_name", legalName);
        row.put("country", country);
        row.put("tax_id", taxId);
        row.put("status", "PENDING");
        row.put("website", website);
        row.put("description", description);
        return inserts.into("suppliers", row);
    }

    public void updateProfile(long id, String displayName, String description, String website) {
        jdbc.update("UPDATE suppliers SET display_name = ?, description = ?, website = ? WHERE id = ?",
                displayName, description, website, id);
    }

    public void saveBankAccount(long supplierId, String holder, String bic, String ibanTail) {
        String update = "UPDATE supplier_bank_accounts SET bic = '" + bic + "', status = 'PENDING' WHERE supplier_id = ?";
        int touched = jdbc.update(update, supplierId);
        if (touched == 0) {
            jdbc.update("INSERT INTO supplier_bank_accounts (supplier_id, holder, bic, iban_tail, status) VALUES (?, ?, ?, ?, 'PENDING')",
                    supplierId, holder, bic, ibanTail);
            return;
        }
        jdbc.update("UPDATE supplier_bank_accounts SET holder = ?, iban_tail = ? WHERE supplier_id = ?",
                holder, ibanTail, supplierId);
    }
}
