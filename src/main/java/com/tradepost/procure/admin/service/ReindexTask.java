package com.tradepost.procure.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("reindex")
public class ReindexTask implements MaintenanceTask {

    private static final Logger log = LoggerFactory.getLogger(ReindexTask.class);

    @Override
    public int run(String cutoff) {
        log.info("reindex requested; the search index is rebuilt by the nightly job");
        return 0;
    }
}
