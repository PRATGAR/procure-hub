package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.integration.repo.InboundRepository;
import com.tradepost.procure.integration.web.IntegrationDtos.ImportDecoded;
import com.tradepost.procure.integration.web.IntegrationDtos.ImportStaged;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ImportService {

    private final InboundRepository batches;
    private final JavaSerialDecoder decoder;
    private final AuditWriter audit;

    public ImportService(InboundRepository batches, JavaSerialDecoder decoder, AuditWriter audit) {
        this.batches = batches;
        this.decoder = decoder;
        this.audit = audit;
    }

    public ImportStaged stage(String format, byte[] payload) {
        if (!"java".equals(format)) {
            throw new InvalidRequestException("unsupported import format");
        }
        long id = batches.insertBatch(HexFormat.of().formatHex(payload));
        audit.append(Actors.current(), "integration.import.staged", "batch " + id + " bytes " + payload.length);
        return new ImportStaged(id, format);
    }

    public ImportDecoded decode(long batchId) {
        byte[] bytes = HexFormat.of().parseHex(batches.batchHex(batchId));
        try {
            Map<String, Object> decoded = decoder.decode(bytes);
            return new ImportDecoded(batchId, String.valueOf(decoded.get("type")), String.valueOf(decoded.get("value")));
        } catch (Exception e) {
            throw new InvalidRequestException("payload could not be decoded");
        }
    }
}
