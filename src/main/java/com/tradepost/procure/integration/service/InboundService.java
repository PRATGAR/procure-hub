package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.Digests;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.integration.Partner;
import com.tradepost.procure.integration.PartnerCode;
import com.tradepost.procure.integration.repo.InboundRepository;
import com.tradepost.procure.integration.repo.PartnerRepository;
import com.tradepost.procure.integration.web.IntegrationDtos.InboundRequest;
import org.springframework.stereotype.Service;

@Service
public class InboundService {

    private final PartnerRepository partners;
    private final InboundRepository inbound;

    public InboundService(PartnerRepository partners, InboundRepository inbound) {
        this.partners = partners;
        this.inbound = inbound;
    }

    public long accept(PartnerCode code, String signature, InboundRequest request) {
        Partner partner = partners.findByCode(code.name());
        String expected = Digests.hex(partner.sigAlg(), partner.sharedSecret() + request.payloadHex());
        if (!expected.equalsIgnoreCase(signature)) {
            throw new InvalidRequestException("signature mismatch");
        }
        return inbound.insert(code.name(), request.eventType(), request.payloadHex(), signature);
    }
}
