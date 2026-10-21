package com.tradepost.procure.integration.web;

import com.tradepost.procure.integration.PartnerCode;
import com.tradepost.procure.integration.service.ImportService;
import com.tradepost.procure.integration.service.InboundService;
import com.tradepost.procure.integration.service.IntegrationService;
import com.tradepost.procure.integration.service.ReplayService;
import com.tradepost.procure.integration.web.IntegrationDtos.CreatePartnerRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.InboundRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.PartnerEventRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.RegisterWebhookRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    private final IntegrationService service;
    private final InboundService inboundService;
    private final ReplayService replayService;
    private final ImportService importService;

    public IntegrationController(IntegrationService service, InboundService inboundService,
                                 ReplayService replayService, ImportService importService) {
        this.service = service;
        this.inboundService = inboundService;
        this.replayService = replayService;
        this.importService = importService;
    }

    @PostMapping("/partners")
    public ResponseEntity<Map<String, Object>> createPartner(@Valid @RequestBody CreatePartnerRequest request) {
        long id = service.createPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "ACTIVE"));
    }

    @GetMapping("/partners/{id}/ping")
    public Map<String, Object> ping(@PathVariable long id) {
        return service.ping(id);
    }

    @PostMapping("/webhooks")
    public ResponseEntity<Map<String, Object>> registerWebhook(@Valid @RequestBody RegisterWebhookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerWebhook(request));
    }

    @PostMapping("/webhooks/{id}/test")
    public Map<String, Object> testWebhook(@PathVariable long id) {
        return service.testWebhook(id);
    }

    @PostMapping("/partners/{code}/events")
    public Map<String, Object> publishEvent(@PathVariable PartnerCode code,
                                            @Valid @RequestBody PartnerEventRequest request) {
        return service.publishEvent(code, request);
    }

    @PostMapping("/inbound/{partner}")
    public ResponseEntity<Map<String, Object>> inbound(@PathVariable("partner") PartnerCode partner,
                                                       @RequestHeader("X-Signature") String signature,
                                                       @Valid @RequestBody InboundRequest request) {
        long id = inboundService.accept(partner, signature, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("inboundId", id));
    }

    @PostMapping("/inbound/{id}/replay")
    public Map<String, Object> replay(@PathVariable long id) {
        return replayService.replay(id);
    }

    @PostMapping(value = "/imports/{format}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Map<String, Object> importPayload(@PathVariable String format, @RequestBody byte[] payload) {
        return importService.submit(format, payload);
    }

    @GetMapping("/partners/{code}/health")
    public Map<String, Object> health(@PathVariable PartnerCode code) {
        return service.health(code);
    }

    @PostMapping("/partners/{code}/sync")
    public Map<String, Object> sync(@PathVariable PartnerCode code) {
        return service.sync(code);
    }
}
