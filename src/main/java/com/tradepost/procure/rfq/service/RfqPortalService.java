package com.tradepost.procure.rfq.service;

import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.NotFoundException;
import com.tradepost.procure.rfq.Invitation;
import com.tradepost.procure.rfq.Rfq;
import com.tradepost.procure.rfq.repo.BidRepository;
import com.tradepost.procure.rfq.repo.InvitationRepository;
import com.tradepost.procure.rfq.repo.RfqRepository;
import com.tradepost.procure.rfq.web.InvitationToken;
import com.tradepost.procure.rfq.web.RfqDtos.BidRequest;
import com.tradepost.procure.rfq.web.RfqDtos.QuestionRequest;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RfqPortalService {

    private final InvitationRepository invitations;
    private final RfqRepository rfqs;
    private final BidRepository bids;

    public RfqPortalService(InvitationRepository invitations, RfqRepository rfqs, BidRepository bids) {
        this.invitations = invitations;
        this.rfqs = rfqs;
        this.bids = bids;
    }

    public Map<String, Object> landing(InvitationToken token) {
        Invitation invitation = invitations.findByToken(token.value());
        Rfq rfq = rfqs.findById(invitation.rfqId());
        if ("SENT".equals(invitation.status())) {
            invitations.updateStatus(invitation.id(), "VIEWED");
        }
        return Map.of("rfqId", rfq.id(), "title", rfq.title(), "status", rfq.status(),
                "closesAt", String.valueOf(rfq.closesAt()), "invitation", invitation.status());
    }

    public void decline(String token) {
        Invitation invitation = invitations.findByToken(token);
        invitations.updateStatus(invitation.id(), "DECLINED");
    }

    public long submitBid(InvitationToken token, String pin, BidRequest request) {
        Invitation invitation = open(token.value());
        if (!invitation.accessPin().equals(pin)) {
            throw new InvalidRequestException("access pin mismatch");
        }
        Rfq rfq = rfqs.findById(invitation.rfqId());
        if (!"OPEN".equals(rfq.status())) {
            throw new ConflictException("the rfq is closed");
        }
        long bidId = bids.insertBid(rfq.id(), invitation.id(), request.amountCents(), request.currency().trim(),
                request.incoterm().name(), request.notes());
        invitations.updateStatus(invitation.id(), "BID_SUBMITTED");
        return bidId;
    }

    public long ask(InvitationToken token, QuestionRequest request) {
        Invitation invitation = open(token.value());
        return bids.insertQuestion(invitation.rfqId(), invitation.id(), request.question());
    }

    private Invitation open(String token) {
        return invitations.findOpenByToken(token).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("invitation", "for this token"));
    }
}
