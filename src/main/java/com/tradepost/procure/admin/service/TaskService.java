package com.tradepost.procure.admin.service;

import com.tradepost.procure.admin.repo.SettingsRepository;
import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final ApplicationEventPublisher events;
    private final SettingsRepository settings;
    private final AuditWriter audit;

    public TaskService(ApplicationEventPublisher events, SettingsRepository settings, AuditWriter audit) {
        this.events = events;
        this.settings = settings;
        this.audit = audit;
    }

    public Map<String, Object> run(String name, String cutoff) {
        AtomicInteger affected = new AtomicInteger();
        events.publishEvent(new TaskRequested(name, cutoff, affected));
        settings.recordRun(name, cutoff, affected.get());
        audit.append(Actors.current(), "admin.task", name);
        return Map.of("task", name, "affected", affected.get());
    }
}
