package com.tradepost.procure.rfq.web;

import com.tradepost.procure.common.NumericId;
import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.rfq.Bid;
import com.tradepost.procure.rfq.Rfq;
import com.tradepost.procure.rfq.service.RfqPages;
import com.tradepost.procure.rfq.service.RfqService;
import com.tradepost.procure.rfq.web.RfqDtos.AwardRequest;
import com.tradepost.procure.rfq.web.RfqDtos.CreateRfqRequest;
import com.tradepost.procure.rfq.web.RfqDtos.InviteRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rfqs")
public class RfqController {

    private final RfqService service;
    private final RfqPages pages;
    private final PageWriter writer;

    public RfqController(RfqService service, RfqPages pages, PageWriter writer) {
        this.service = service;
        this.pages = pages;
        this.writer = writer;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateRfqRequest request) {
        long id = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "OPEN"));
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Rfq get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/invitations")
    public List<Map<String, Object>> invite(@PathVariable long id, @Valid @RequestBody InviteRequest request) {
        return service.invite(id, request);
    }

    @GetMapping("/{id}/regional-peers")
    public List<Map<String, Object>> regionalPeers(@PathVariable long id) {
        return service.regionalPeers(id);
    }

    @GetMapping("/{id}/bids/summary")
    public void bidsSummary(@PathVariable long id, HttpServletResponse response) throws IOException {
        Rfq rfq = service.get(id);
        writer.html(response, pages.bids(rfq, service.bidList(id)));
    }

    @GetMapping("/{id}/bids/{bidId}/duplicates")
    public List<Bid> duplicateBids(@PathVariable long id, @PathVariable long bidId) {
        return service.duplicateBids(id, bidId);
    }

    @GetMapping("/{id}/questions/view")
    public void questionsView(@PathVariable long id, HttpServletResponse response) throws IOException {
        Rfq rfq = service.get(id);
        writer.html(response, pages.questions(rfq, service.questions(id)));
    }

    @PostMapping("/{id}/award")
    public Map<String, Object> award(@PathVariable long id, @Valid @RequestBody AwardRequest request) {
        return service.award(id, request);
    }

    @GetMapping("/{id}/bids/{bidId}")
    public Bid bid(@PathVariable NumericId id, @PathVariable NumericId bidId) {
        return service.bid(id, bidId);
    }
}
