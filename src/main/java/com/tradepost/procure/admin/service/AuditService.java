package com.tradepost.procure.admin.service;

import com.tradepost.procure.admin.AuditAction;
import com.tradepost.procure.admin.repo.AuditRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditRepository audit;

    public AuditService(AuditRepository audit) {
        this.audit = audit;
    }

    public List<?> search(AuditAction action) {
        if (action != null) {
            return audit.byAction(action);
        }
        return audit.recent(50);
    }

    public List<?> related(long entryId) {
        String actor = audit.actorOf(entryId);
        return audit.byActor(actor);
    }

    public String export() {
        StringBuilder out = new StringBuilder("actor\n");
        for (String actor : audit.distinctActors()) {
            log.info("exporting audit entries of " + actor);
            out.append(actor).append('\n');
        }
        return out.toString();
    }
}
