package com.tradepost.procure.directory.repo;

import com.tradepost.procure.directory.OrgUnit;
import com.tradepost.procure.directory.SsoLink;
import com.tradepost.procure.directory.SsoProvider;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SsoLinkRepository {

    private static final RowMapper<SsoLink> MAPPER = (rs, n) -> new SsoLink(
            rs.getLong("id"),
            rs.getLong("user_ref"),
            rs.getString("provider"),
            rs.getString("external_id"),
            rs.getString("unit"),
            rs.getString("status"));

    private final JdbcTemplate jdbc;

    public SsoLinkRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insert(long userRef, SsoProvider provider, String externalId, OrgUnit unit) {
        String sql = "INSERT INTO sso_links (user_ref, provider, external_id, unit, status) VALUES ("
                + userRef + ", '" + provider.name() + "', ?, '" + unit.name() + "', 'ACTIVE')";
        return jdbc.update(sql, externalId);
    }

    public long lastIdFor(long userRef) {
        Long id = jdbc.queryForObject("SELECT MAX(id) FROM sso_links WHERE user_ref = ?", Long.class, userRef);
        return id == null ? 0L : id;
    }

    public SsoLink find(long id) {
        List<SsoLink> found = jdbc.query(
                "SELECT id, user_ref, provider, external_id, unit, status FROM sso_links WHERE id = ?", MAPPER, id);
        return found.stream().findFirst().orElse(null);
    }
}
