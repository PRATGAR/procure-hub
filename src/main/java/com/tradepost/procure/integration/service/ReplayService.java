package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.integration.repo.InboundRepository;
import com.tradepost.procure.integration.web.IntegrationDtos.Replayed;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class ReplayService {

    private final InboundRepository inbound;

    public ReplayService(InboundRepository inbound) {
        this.inbound = inbound;
    }

    public Replayed replay(long inboundId) {
        byte[] bytes = HexFormat.of().parseHex(inbound.payloadHex(inboundId));
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            in.setObjectInputFilter(SerialFilters.partnerEvents());
            Object event = in.readObject();
            return new Replayed(inboundId, String.valueOf(event));
        } catch (IOException | ClassNotFoundException e) {
            throw new InvalidRequestException("stored payload could not be replayed");
        }
    }
}
