package com.tradepost.procure.admin.repo;

import com.tradepost.procure.admin.AuditAction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditRepository {

    @PersistenceContext
    private EntityManager em;

    private final JdbcTemplate jdbc;

    public AuditRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<?> byActor(String actor) {
        String sql = "SELECT id, actor, action, detail FROM audit_log WHERE actor = '" + actor + "' ORDER BY id DESC";
        Query actorQuery = em.createNativeQuery(sql);
        return actorQuery.getResultList();
    }

    public List<?> byAction(AuditAction action) {
        String sql = "SELECT id, actor, action, detail FROM audit_log WHERE action = '" + action.code() + "' ORDER BY id DESC";
        Query actionQuery = em.createNativeQuery(sql);
        return actionQuery.getResultList();
    }

    public List<Map<String, Object>> recent(int limit) {
        return jdbc.queryForList("SELECT id, actor, action, detail FROM audit_log ORDER BY id DESC LIMIT ?", limit);
    }

    public List<String> distinctActors() {
        return jdbc.queryForList("SELECT DISTINCT actor FROM audit_log ORDER BY actor", String.class);
    }
}
