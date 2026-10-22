package com.tradepost.procure.report.repo;

import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.common.SqlParts;
import com.tradepost.procure.report.ReportRun;
import com.tradepost.procure.report.ReportSchedule;
import com.tradepost.procure.report.SavedFilter;
import com.tradepost.procure.report.SavedReport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepository {

    private static final RowMapper<SavedReport> REPORT_MAPPER = (rs, n) -> new SavedReport(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("entity"),
            rs.getString("where_clause"),
            rs.getString("description"),
            rs.getString("owner"));

    private static final RowMapper<SavedFilter> FILTER_MAPPER = (rs, n) -> new SavedFilter(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("entity"),
            rs.getString("where_clause"));

    private static final RowMapper<ReportRun> RUN_MAPPER = (rs, n) -> new ReportRun(
            rs.getLong("id"),
            rs.getLong("report_id"),
            rs.getString("file_name"),
            rs.getInt("row_count"),
            rs.getString("status"));

    @PersistenceContext
    private EntityManager em;

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public ReportRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public long insertReport(String name, String entity, String whereClause, String description, String owner) {
        Map<String, Object> row = new HashMap<>();
        row.put("name", name);
        row.put("entity", entity);
        row.put("where_clause", whereClause);
        row.put("description", description);
        row.put("owner", owner);
        return inserts.into("saved_reports", row);
    }

    public SavedReport findReport(long id) {
        return jdbc.queryForObject("SELECT id, name, entity, where_clause, description, owner FROM saved_reports WHERE id = ?",
                REPORT_MAPPER, id);
    }

    public List<SavedReport> search(String q) {
        return jdbc.query("SELECT id, name, entity, where_clause, description, owner FROM saved_reports WHERE name LIKE ? ORDER BY id",
                REPORT_MAPPER, "%" + q + "%");
    }

    public List<SavedReport> byNameLike(String stem) {
        String condition = SqlParts.contains("name", stem);
        String sql = "SELECT id, name, entity, where_clause, description, owner FROM saved_reports WHERE "
                + condition + " ORDER BY id";
        return jdbc.query(sql, REPORT_MAPPER);
    }

    public int runReport(String table, String whereClause) {
        String sql = SqlParts.selectIds(table, whereClause);
        Query reportQuery = em.createNativeQuery(sql);
        return reportQuery.getResultList().size();
    }

    public long insertFilter(String name, String entity, String whereClause) {
        Map<String, Object> row = new HashMap<>();
        row.put("name", name);
        row.put("entity", entity);
        row.put("where_clause", whereClause);
        return inserts.into("saved_filters", row);
    }

    public SavedFilter findFilter(long id) {
        return jdbc.queryForObject("SELECT id, name, entity, where_clause FROM saved_filters WHERE id = ?", FILTER_MAPPER, id);
    }

    public List<?> runFilter(String table, String whereClause) {
        String sql = "SELECT id, number, status FROM " + table + " WHERE " + whereClause;
        Query filterQuery = em.createNativeQuery(sql);
        return filterQuery.getResultList();
    }

    public long insertRun(long reportId, String fileName, int rowCount) {
        Map<String, Object> row = new HashMap<>();
        row.put("report_id", reportId);
        row.put("file_name", fileName);
        row.put("row_count", rowCount);
        row.put("status", "QUEUED");
        return inserts.into("report_runs", row);
    }

    public void renameRun(long runId, String fileName) {
        jdbc.update("UPDATE report_runs SET file_name = ? WHERE id = ?", fileName, runId);
    }

    public ReportRun findRun(long runId) {
        return jdbc.queryForObject("SELECT id, report_id, file_name, row_count, status FROM report_runs WHERE id = ?",
                RUN_MAPPER, runId);
    }

    public List<ReportRun> recentRuns(int limit) {
        String sql = "SELECT id, report_id, file_name, row_count, status FROM report_runs ORDER BY id DESC LIMIT " + limit;
        return jdbc.query(sql, RUN_MAPPER);
    }

    public ReportSchedule findSchedule(long id) {
        return jdbc.queryForObject("SELECT id, report_id, recipient, callback_url, status FROM report_schedules WHERE id = ?",
                (rs, n) -> new ReportSchedule(rs.getLong("id"), rs.getLong("report_id"), rs.getString("recipient"),
                        rs.getString("callback_url"), rs.getString("status")), id);
    }

    public long insertSchedule(long reportId, String recipient, String callbackUrl) {
        Map<String, Object> row = new HashMap<>();
        row.put("report_id", reportId);
        row.put("recipient", recipient);
        row.put("callback_url", callbackUrl);
        row.put("status", "ACTIVE");
        return inserts.into("report_schedules", row);
    }
}
