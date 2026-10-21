package com.tradepost.procure.admin.service;

import com.tradepost.procure.admin.AuditAction;
import com.tradepost.procure.admin.repo.AuditRepository;
import com.tradepost.procure.common.Aggregates;
import com.tradepost.procure.report.ReportEntity;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditRepository audit;
    private final Aggregates aggregates;

    public AuditService(AuditRepository audit, Aggregates aggregates) {
        this.audit = audit;
        this.aggregates = aggregates;
    }

    public List<?> search(String actor, AuditAction action) {
        if (actor != null && !actor.isBlank()) {
            return audit.byActor(actor);
        }
        if (action != null) {
            return audit.byAction(action);
        }
        return audit.recent(50);
    }

    public String export() {
        StringBuilder out = new StringBuilder("actor\n");
        for (String actor : audit.distinctActors()) {
            log.info("exporting audit entries of " + actor);
            out.append(actor).append('\n');
        }
        return out.toString();
    }

    public List<Map<String, Object>> stats(ReportEntity entity, String by) {
        return aggregates.countBy(entity.table(), by);
    }
}
