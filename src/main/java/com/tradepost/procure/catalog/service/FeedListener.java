package com.tradepost.procure.catalog.service;

import com.tradepost.procure.common.InvalidRequestException;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FeedListener {

    private final Map<String, FeedParser> parsers;

    public FeedListener(Map<String, FeedParser> parsers) {
        this.parsers = parsers;
    }

    @EventListener
    public void onFeedReceived(FeedReceived event) {
        FeedParser parser = parsers.get(event.format());
        if (parser == null) {
            throw new InvalidRequestException("unsupported feed format");
        }
        try {
            event.result().add(parser.parse(event.payload()));
        } catch (Exception e) {
            throw new InvalidRequestException("feed could not be processed");
        }
    }
}
