package com.tradepost.procure.catalog.service;

import com.tradepost.procure.catalog.Feed;
import com.tradepost.procure.catalog.repo.CatalogRepository;
import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.InvalidRequestException;
import org.springframework.stereotype.Service;

@Service
public class CatalogImportService {

    private final CatalogRepository repo;
    private final XmlFeedParser xmlParser;
    private final CsvFeedParser csvParser;
    private final AuditWriter audit;

    public CatalogImportService(CatalogRepository repo, XmlFeedParser xmlParser, CsvFeedParser csvParser,
                                AuditWriter audit) {
        this.repo = repo;
        this.xmlParser = xmlParser;
        this.csvParser = csvParser;
        this.audit = audit;
    }

    public long stage(String format, String payload) {
        if (!"xml".equals(format) && !"csv".equals(format)) {
            throw new InvalidRequestException("unsupported feed format");
        }
        long id = repo.insertFeed(format, payload);
        audit.append(Actors.current(), "catalog.feed.staged", "feed " + id + " format " + format);
        return id;
    }

    public int process(long feedId) {
        Feed feed = repo.findFeed(feedId);
        FeedParser parser = "xml".equals(feed.format()) ? xmlParser : csvParser;
        try {
            int imported = parser.parse(feed.payload());
            audit.append(Actors.current(), "catalog.import", "feed " + feedId + " items " + imported);
            return imported;
        } catch (Exception e) {
            throw new InvalidRequestException("feed could not be processed");
        }
    }
}
