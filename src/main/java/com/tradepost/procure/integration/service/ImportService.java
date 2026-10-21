package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ImportService {

    private final ApplicationEventPublisher events;
    private final AuditWriter audit;

    public ImportService(ApplicationEventPublisher events, AuditWriter audit) {
        this.events = events;
        this.audit = audit;
    }

    public Map<String, Object> submit(String format, byte[] payload) {
        AtomicReference<Map<String, Object>> result = new AtomicReference<>(Map.of());
        events.publishEvent(new ImportSubmitted(format, payload, result));
        audit.append(Actors.current(), "integration.import", "format " + format + " bytes " + payload.length);
        return result.get();
    }
}
