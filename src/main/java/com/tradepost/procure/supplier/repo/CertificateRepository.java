package com.tradepost.procure.supplier.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.supplier.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CertificateRepository {

    private static final RowMapper<Certificate> MAPPER = (rs, n) -> new Certificate(
            rs.getLong("id"),
            rs.getLong("supplier_id"),
            rs.getString("issuer"),
            rs.getString("file_name"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public CertificateRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insert(long supplierId, String issuer, String fileName) {
        Map<String, Object> row = new HashMap<>();
        row.put("supplier_id", supplierId);
        row.put("issuer", issuer);
        row.put("file_name", fileName);
        return inserts.into("supplier_certificates", row);
    }

    public Certificate find(long supplierId, long certificateId) {
        return jdbc.queryForObject(
                "SELECT id, supplier_id, issuer, file_name FROM supplier_certificates WHERE id = ? AND supplier_id = ?",
                MAPPER, certificateId, supplierId);
    }

    public List<Certificate> forSupplier(long supplierId) {
        return jdbc.query("SELECT id, supplier_id, issuer, file_name FROM supplier_certificates WHERE supplier_id = ? ORDER BY id",
                MAPPER, supplierId);
    }
}
