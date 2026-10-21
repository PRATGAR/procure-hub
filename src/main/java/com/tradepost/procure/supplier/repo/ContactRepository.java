package com.tradepost.procure.supplier.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.common.Terms;
import com.tradepost.procure.supplier.Contact;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ContactRepository {

    private static final RowMapper<Contact> MAPPER = (rs, n) -> new Contact(
            rs.getLong("id"),
            rs.getLong("supplier_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("role"),
            rs.getString("note"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public ContactRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insert(long supplierId, String name, String email, String role, String note) {
        Map<String, Object> row = new HashMap<>();
        row.put("supplier_id", supplierId);
        row.put("name", name);
        row.put("email", email);
        row.put("role", role);
        row.put("note", note);
        return inserts.into("supplier_contacts", row);
    }

    public List<Contact> forSupplier(long supplierId) {
        return jdbc.query("SELECT id, supplier_id, name, email, role, note FROM supplier_contacts WHERE supplier_id = ? ORDER BY name",
                MAPPER, supplierId);
    }

    public List<Contact> search(long supplierId, String q) {
        String sql = "SELECT id, supplier_id, name, email, role, note FROM supplier_contacts WHERE supplier_id = ? AND name LIKE '%"
                + Terms.normalize(q) + "%' ORDER BY name";
        return jdbc.query(sql, MAPPER, supplierId);
    }
}
