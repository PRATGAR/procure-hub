package com.tradepost.procure.rfq.web;

import com.tradepost.procure.rfq.service.RfqPortalService;
import com.tradepost.procure.rfq.web.RfqDtos.BidRequest;
import com.tradepost.procure.rfq.web.RfqDtos.QuestionRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/rfq")
public class RfqPortalController {

    private final RfqPortalService service;

    public RfqPortalController(RfqPortalService service) {
        this.service = service;
    }

    @GetMapping("/{token}")
    public Map<String, Object> landing(@PathVariable InvitationToken token) {
        return service.landing(token);
    }

    @PostMapping("/decline")
    public Map<String, Object> decline(@RequestHeader("X-Invite-Token") String token) {
        service.decline(token);
        return Map.of("status", "DECLINED");
    }

    @PostMapping("/{token}/bids")
    public ResponseEntity<Map<String, Object>> bid(@PathVariable InvitationToken token,
                                                   @RequestHeader("X-Access-Pin") String pin,
                                                   @Valid @RequestBody BidRequest request) {
        long id = service.submitBid(token, pin, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("bidId", id));
    }

    @PostMapping("/{token}/questions")
    public ResponseEntity<Map<String, Object>> ask(@PathVariable InvitationToken token,
                                                   @Valid @RequestBody QuestionRequest request) {
        long id = service.ask(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("questionId", id));
    }
}
