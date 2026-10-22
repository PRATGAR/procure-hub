package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.Codes;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.NotFoundException;
import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.integration.Partner;
import com.tradepost.procure.integration.PartnerCode;
import com.tradepost.procure.integration.Webhook;
import com.tradepost.procure.integration.repo.PartnerRepository;
import com.tradepost.procure.integration.web.IntegrationDtos.ContractSaved;
import com.tradepost.procure.integration.web.IntegrationDtos.CreatePartnerRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.EventPublished;
import com.tradepost.procure.integration.web.IntegrationDtos.PartnerEventRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.PingResult;
import com.tradepost.procure.integration.web.IntegrationDtos.RegisterWebhookRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.WebhookRegistered;
import com.tradepost.procure.integration.web.IntegrationDtos.WebhookTested;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class IntegrationService {

    private final PartnerRepository partners;
    private final PartnerUrls urls;
    private final OutboundHttp outbound;
    private final AuditWriter audit;
    private final Random secrets;
    private final ContractFiles contracts;

    public IntegrationService(PartnerRepository partners, PartnerUrls urls, OutboundHttp outbound, AuditWriter audit,
                              @Qualifier("webhookSecrets") Random secrets, ContractFiles contracts) {
        this.contracts = contracts;
        this.partners = partners;
        this.urls = urls;
        this.outbound = outbound;
        this.audit = audit;
        this.secrets = secrets;
    }

    public long createPartner(CreatePartnerRequest request) {
        if (partners.codeTaken(request.code().name())) {
            throw new ConflictException("the partner code is already registered");
        }
        String secret = HexFormat.of().formatHex(new SecureRandom().generateSeed(12));
        String sigAlg = request.sigAlg() == null ? "SHA-256" : request.sigAlg();
        long id = partners.insert(request.code().name(), request.name().trim(), request.baseUrl(),
                request.portalUrl(), sigAlg, secret);
        audit.append(Actors.current(), "partner.created", request.code().name());
        return id;
    }

    public Partner partner(long id) {
        return partners.find(id);
    }

    public Partner partnerByCode(PartnerCode code) {
        return partners.findByCode(code.name());
    }

    public PingResult ping(long partnerId) {
        Partner partner = partners.find(partnerId);
        String body = outbound.fetchText(partner.baseUrl() + "/ping");
        return new PingResult(partner.code(), body == null ? 0 : body.length());
    }

    public WebhookRegistered registerWebhook(RegisterWebhookRequest request) {
        partners.find(request.partnerId());
        String secret = Codes.hex(secrets, 8);
        long id = partners.insertWebhook(request.partnerId(), request.url().trim(), request.eventType(), secret);
        audit.append(Actors.current(), "webhook.registered", "webhook " + id);
        return new WebhookRegistered(id, secret);
    }

    public WebhookTested testWebhook(long id) {
        Webhook webhook = partners.findWebhook(id);
        Map<String, Object> payload = Map.of("event", "webhook.test", "webhookId", webhook.id());
        try {
            String body = outbound.deliver(webhook.url(), payload);
            partners.recordDelivery(id, 200, body == null ? "" : body.substring(0, Math.min(body.length(), 180)));
            return new WebhookTested(id, true);
        } catch (RestClientException e) {
            partners.recordDelivery(id, 599, "delivery failed");
            return new WebhookTested(id, false);
        }
    }

    public EventPublished publishEvent(PartnerCode code, PartnerEventRequest request) {
        String body = outbound.deliver(urls.events(code),
                Map.of("eventType", request.eventType(), "body", request.body() == null ? "" : request.body()));
        return new EventPublished(code.name(), body != null);
    }

    public PingResult health(PartnerCode code) {
        String body = outbound.fetchText(urls.health(code));
        return new PingResult(code.name(), body == null ? 0 : body.length());
    }

    public PingResult sync(PartnerCode code) {
        byte[] catalog = outbound.download(urls.catalog(code));
        return new PingResult(code.name(), catalog == null ? 0 : catalog.length);
    }

    public ContractSaved saveContractFile(long partnerId, String fileName) {
        partners.find(partnerId);
        String name = fileName.trim();
        if (name.startsWith("/") || name.startsWith("\\") || Path.of(name).isAbsolute()) {
            throw new InvalidRequestException("contract files are stored relative to the document folder");
        }
        partners.updateContractFile(partnerId, name);
        audit.append(Actors.current(), "partner.contract.saved", "partner " + partnerId);
        return new ContractSaved(partnerId, true);
    }

    public byte[] contract(long partnerId) throws IOException {
        partners.find(partnerId);
        String fileName = partners.contractFile(partnerId);
        if (fileName == null || fileName.isBlank()) {
            throw new NotFoundException("contract for partner", partnerId);
        }
        return contracts.open(fileName);
    }

    public String docsUrl(PartnerCode code) {
        return urls.docs(code);
    }
}
