package com.tradepost.procure.catalog.service;

import org.springframework.stereotype.Component;

@Component
public class CsvFeedParser implements FeedParser {

    private final FeedItems items;

    public CsvFeedParser(FeedItems items) {
        this.items = items;
    }

    @Override
    public int parse(String payload) {
        int imported = 0;
        for (String line : payload.split("\\R")) {
            String[] cols = line.split(";");
            if (cols.length < 5) {
                continue;
            }
            long price;
            try {
                price = Long.parseLong(cols[3].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            if (items.accept(cols[0].trim(), cols[1].trim(), cols[2].trim(), price, cols[4].trim())) {
                imported++;
            }
        }
        return imported;
    }
}
