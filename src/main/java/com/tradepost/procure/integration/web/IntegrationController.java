package com.tradepost.procure.integration.web;

import com.tradepost.procure.integration.PartnerCode;
import com.tradepost.procure.integration.service.ImportService;
import com.tradepost.procure.integration.service.InboundService;
import com.tradepost.procure.integration.service.IntegrationService;
import com.tradepost.procure.integration.service.ReplayService;
import com.tradepost.procure.integration.web.IntegrationDtos.ContractRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.ContractSaved;
import com.tradepost.procure.integration.web.IntegrationDtos.CreatePartnerRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.Created;
import com.tradepost.procure.integration.web.IntegrationDtos.EventPublished;
import com.tradepost.procure.integration.web.IntegrationDtos.ImportDecoded;
import com.tradepost.procure.integration.web.IntegrationDtos.ImportStaged;
import com.tradepost.procure.integration.web.IntegrationDtos.InboundAccepted;
import com.tradepost.procure.integration.web.IntegrationDtos.InboundRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.PartnerEventRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.PingResult;
import com.tradepost.procure.integration.web.IntegrationDtos.RegisterWebhookRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.Replayed;
import com.tradepost.procure.integration.web.IntegrationDtos.WebhookRegistered;
import com.tradepost.procure.integration.web.IntegrationDtos.WebhookTested;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    public ResponseEntity<Created> createPartner(@Valid @RequestBody CreatePartnerRequest request) {
        long id = service.createPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(id, "ACTIVE"));
    }

    @GetMapping("/partners/{id}/ping")
    public PingResult ping(@PathVariable long id) {
        return service.ping(id);
    }

    @PutMapping("/partners/{id}/contract-file")
    public ContractSaved saveContractFile(@PathVariable long id, @Valid @RequestBody ContractRequest request) {
        return service.saveContractFile(id, request.fileName());
    }

    @GetMapping("/partners/{id}/contract")
    public byte[] contract(@PathVariable long id, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return service.contract(id);
    }

    @PostMapping("/webhooks")
    public ResponseEntity<WebhookRegistered> registerWebhook(@Valid @RequestBody RegisterWebhookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerWebhook(request));
    }

    @PostMapping("/webhooks/{id}/test")
    public WebhookTested testWebhook(@PathVariable long id) {
        return service.testWebhook(id);
    }

    @PostMapping("/partners/{code}/events")
    public EventPublished publishEvent(@PathVariable PartnerCode code,
                                       @Valid @RequestBody PartnerEventRequest request) {
        return service.publishEvent(code, request);
    }

    @PostMapping("/inbound/{partner}")
    public ResponseEntity<InboundAccepted> inbound(@PathVariable("partner") PartnerCode partner,
                                                   @RequestHeader("X-Signature") String signature,
                                                   @Valid @RequestBody InboundRequest request) {
        long id = inboundService.accept(partner, signature, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new InboundAccepted(id));
    }

    @PostMapping("/inbound/{id}/replay")
    public Replayed replay(@PathVariable long id) {
        return replayService.replay(id);
    }

    @PostMapping(value = "/imports/{format}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ImportStaged stageImport(@PathVariable String format, @RequestBody byte[] payload) {
        return importService.stage(format, payload);
    }

    @PostMapping("/imports/{batchId}/decode")
    public ImportDecoded decodeImport(@PathVariable long batchId) {
        return importService.decode(batchId);
    }

    @GetMapping("/partners/{code}/health")
    public PingResult health(@PathVariable PartnerCode code) {
        return service.health(code);
    }

    @PostMapping("/partners/{code}/sync")
    public PingResult sync(@PathVariable PartnerCode code) {
        return service.sync(code);
    }
}
