package com.tradepost.procure.document.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.document.Document;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {

    private static final RowMapper<Document> MAPPER = (rs, n) -> new Document(
            rs.getLong("id"),
            rs.getString("owner_type"),
            rs.getLong("owner_id"),
            rs.getString("file_name"),
            rs.getString("display_name"),
            rs.getString("convert_opts"),
            rs.getString("archive_name"),
            rs.getString("fingerprint"),
            rs.getString("status"));

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public DocumentRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public boolean fingerprintKnown(String fingerprint) {
        Integer known = jdbc.queryForObject("SELECT COUNT(*) FROM documents WHERE fingerprint = ?", Integer.class, fingerprint);
        return known != null && known > 0;
    }

    public long insert(Document document) {
        Map<String, Object> row = new HashMap<>();
        row.put("owner_type", document.ownerType());
        row.put("owner_id", document.ownerId());
        row.put("file_name", document.fileName());
        row.put("display_name", document.displayName());
        row.put("convert_opts", document.convertOpts());
        row.put("archive_name", document.archiveName());
        row.put("fingerprint", document.fingerprint());
        row.put("status", "REGISTERED");
        return inserts.into("documents", row);
    }

    public Document find(long id) {
        return jdbc.queryForObject(
                "SELECT id, owner_type, owner_id, file_name, display_name, convert_opts, archive_name, fingerprint, status "
                        + "FROM documents WHERE id = ?", MAPPER, id);
    }

    public void insertAttachment(String ownerType, long ownerId, String originalName, String storedName) {
        jdbc.update("INSERT INTO document_attachments (owner_type, owner_id, original_name, stored_name) VALUES (?, ?, ?, ?)",
                ownerType, ownerId, originalName, storedName);
    }
}
