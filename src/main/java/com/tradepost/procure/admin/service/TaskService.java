package com.tradepost.procure.admin.service;

import com.tradepost.procure.admin.repo.SettingsRepository;
import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.InvalidRequestException;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final PurgeTask purgeTask;
    private final ReindexTask reindexTask;
    private final SettingsRepository settings;
    private final AuditWriter audit;

    public TaskService(PurgeTask purgeTask, ReindexTask reindexTask, SettingsRepository settings,
                       AuditWriter audit) {
        this.purgeTask = purgeTask;
        this.reindexTask = reindexTask;
        this.settings = settings;
        this.audit = audit;
    }

    public Map<String, Object> schedule(String name, String cutoff) {
        requireKnown(name);
        settings.saveCutoff(name, cutoff);
        audit.append(Actors.current(), "admin.task.scheduled", name);
        return Map.of("task", name, "scheduled", true);
    }

    public Map<String, Object> run(String name) {
        requireKnown(name);
        String cutoff = settings.cutoff(name)
                .orElseThrow(() -> new InvalidRequestException("no cutoff is scheduled for this task"));
        int affected = "purge".equals(name) ? purgeTask.run(cutoff) : reindexTask.run(cutoff);
        settings.recordRun(name, cutoff, affected);
        audit.append(Actors.current(), "admin.task", name);
        return Map.of("task", name, "affected", affected);
    }

    private void requireKnown(String name) {
        if (!"purge".equals(name) && !"reindex".equals(name)) {
            throw new InvalidRequestException("unknown maintenance task");
        }
    }
}
