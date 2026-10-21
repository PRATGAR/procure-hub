package com.tradepost.procure.catalog.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CatalogImportService {

    private final ApplicationEventPublisher events;
    private final AuditWriter audit;

    public CatalogImportService(ApplicationEventPublisher events, AuditWriter audit) {
        this.events = events;
        this.audit = audit;
    }

    public int start(String format, String payload) {
        FeedResult result = new FeedResult();
        events.publishEvent(new FeedReceived(format, payload, result));
        audit.append(Actors.current(), "catalog.import", "format " + format + " items " + result.imported());
        return result.imported();
    }
}
