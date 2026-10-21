package com.tradepost.procure.admin.service;

import com.tradepost.procure.admin.PurgeTarget;
import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.FileStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class PurgeService {

    private final FileStore store;
    private final AuditWriter audit;

    public PurgeService(FileStore store, AuditWriter audit) {
        this.store = store;
        this.audit = audit;
    }

    public Map<String, Object> purge(PurgeTarget target, int retentionDays) throws IOException {
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        Path directory = store.root().resolve(target.area());
        int deleted = 0;
        try (Stream<Path> files = Files.walk(directory)) {
            List<Path> regular = files.filter(Files::isRegularFile).toList();
            for (Path file : regular) {
                if (Files.getLastModifiedTime(file).toInstant().isBefore(cutoff)) {
                    Files.delete(file);
                    deleted++;
                }
            }
        }
        audit.append(Actors.current(), "admin.purge", target.name() + " " + deleted);
        return Map.of("target", target.name(), "deleted", deleted);
    }
}
