package com.tradepost.procure.rfq.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.Codes;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.NumericId;
import com.tradepost.procure.rfq.Bid;
import com.tradepost.procure.rfq.Question;
import com.tradepost.procure.rfq.Rfq;
import com.tradepost.procure.rfq.repo.BidRepository;
import com.tradepost.procure.rfq.repo.InvitationRepository;
import com.tradepost.procure.rfq.repo.RfqRepository;
import com.tradepost.procure.rfq.web.RfqDtos.AwardRequest;
import com.tradepost.procure.rfq.web.RfqDtos.CreateRfqRequest;
import com.tradepost.procure.rfq.web.RfqDtos.InviteRequest;
import com.tradepost.procure.supplier.service.SupplierService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RfqService {

    private final RfqRepository rfqs;
    private final InvitationRepository invitations;
    private final BidRepository bids;
    private final SupplierService suppliers;
    private final AuditWriter audit;
    private final Random pins;
    private final Random tokens;

    public RfqService(RfqRepository rfqs, InvitationRepository invitations, BidRepository bids,
                      SupplierService suppliers, AuditWriter audit, @Qualifier("invitationPins") Random pins,
                      @Qualifier("invitationTokens") Random tokens) {
        this.rfqs = rfqs;
        this.invitations = invitations;
        this.bids = bids;
        this.suppliers = suppliers;
        this.audit = audit;
        this.pins = pins;
        this.tokens = tokens;
    }

    public long create(CreateRfqRequest request) {
        long id = rfqs.insert(request.title().trim(), request.description(), request.categoryCode(),
                request.region().trim(), request.closesAt(), Actors.current());
        audit.append(Actors.current(), "rfq.created", "rfq " + id);
        return id;
    }

    public List<Map<String, Object>> list() {
        return rfqs.listAll();
    }

    public List<Map<String, Object>> regionalPeers(long id) {
        Rfq rfq = get(id);
        return rfqs.listForRegion(rfq.region());
    }

    public Rfq get(long id) {
        return rfqs.findById(id);
    }

    public List<Map<String, Object>> invite(long rfqId, InviteRequest request) {
        Rfq rfq = get(rfqId);
        if (!"OPEN".equals(rfq.status())) {
            throw new ConflictException("the rfq is no longer open for invitations");
        }
        List<Map<String, Object>> issued = new ArrayList<>();
        for (Long supplierId : request.supplierIds()) {
            suppliers.get(supplierId);
            String token = Codes.hex(tokens, 16);
            String pin = Codes.digits(pins, 6);
            long invitationId = invitations.insert(rfqId, supplierId, token, pin);
            issued.add(Map.of("invitationId", invitationId, "supplierId", supplierId, "token", token));
        }
        audit.append(Actors.current(), "rfq.invited", "rfq " + rfqId + " suppliers " + issued.size());
        return issued;
    }

    public List<Question> questions(long rfqId) {
        return bids.questions(rfqId);
    }

    public List<Bid> bidList(long rfqId) {
        return bids.forRfq(rfqId);
    }

    public List<Bid> duplicateBids(long rfqId, long bidId) {
        Bid bid = bids.findBound(rfqId, bidId);
        return bids.duplicatesByNotes(rfqId, bid.id(), bid.notes() == null ? "" : bid.notes());
    }

    public Bid bid(NumericId rfqId, NumericId bidId) {
        return bids.findForRfq(rfqId.value(), bidId.value());
    }

    public Map<String, Object> award(long rfqId, AwardRequest request) {
        Rfq rfq = get(rfqId);
        if ("AWARDED".equals(rfq.status())) {
            throw new ConflictException("the rfq has already been awarded");
        }
        Bid bid = bids.findBound(rfqId, request.bidId());
        long rate = bids.rateMicro(bid.currency());
        int surchargeBp = bids.surchargeBp(bid.incoterm());
        long amountEur = bid.amountCents() * rate / 1_000_000L;
        long landed = amountEur + amountEur * surchargeBp / 10_000L;
        rfqs.markAwarded(rfqId, request.bidId());
        audit.append(Actors.current(), "rfq.awarded", "rfq " + rfqId + " bid " + request.bidId());
        return Map.of("rfqId", rfqId, "bidId", request.bidId(), "amountEurCents", amountEur, "landedEurCents", landed);
    }
}
