package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.integration.Partner;
import com.tradepost.procure.integration.PartnerCode;
import com.tradepost.procure.integration.Webhook;
import com.tradepost.procure.integration.repo.PartnerRepository;
import com.tradepost.procure.integration.web.IntegrationDtos.CreatePartnerRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.PartnerEventRequest;
import com.tradepost.procure.integration.web.IntegrationDtos.RegisterWebhookRequest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class IntegrationService {

    private final PartnerRepository partners;
    private final PartnerUrls urls;
    private final OutboundHttp outbound;
    private final AuditWriter audit;

    public IntegrationService(PartnerRepository partners, PartnerUrls urls, OutboundHttp outbound, AuditWriter audit) {
        this.partners = partners;
        this.urls = urls;
        this.outbound = outbound;
        this.audit = audit;
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

    public Map<String, Object> ping(long partnerId) {
        Partner partner = partners.find(partnerId);
        String body = outbound.fetchText(partner.baseUrl() + "/ping");
        return Map.of("partner", partner.code(), "bytes", body == null ? 0 : body.length());
    }

    public Map<String, Object> registerWebhook(RegisterWebhookRequest request) {
        partners.find(request.partnerId());
        String secret = Long.toHexString(new Random().nextLong());
        long id = partners.insertWebhook(request.partnerId(), request.url().trim(), request.eventType(), secret);
        audit.append(Actors.current(), "webhook.registered", "webhook " + id);
        return Map.of("id", id, "secret", secret);
    }

    public Map<String, Object> testWebhook(long id) {
        Webhook webhook = partners.findWebhook(id);
        Map<String, Object> payload = Map.of("event", "webhook.test", "webhookId", webhook.id());
        try {
            String body = outbound.deliver(webhook.url(), payload);
            partners.recordDelivery(id, 200, body == null ? "" : body.substring(0, Math.min(body.length(), 180)));
            return Map.of("webhookId", id, "delivered", true);
        } catch (RestClientException e) {
            partners.recordDelivery(id, 599, "delivery failed");
            return Map.of("webhookId", id, "delivered", false);
        }
    }

    public Map<String, Object> publishEvent(PartnerCode code, PartnerEventRequest request) {
        String body = outbound.deliver(urls.events(code),
                Map.of("eventType", request.eventType(), "body", request.body() == null ? "" : request.body()));
        return Map.of("partner", code.name(), "accepted", body != null);
    }

    public Map<String, Object> health(PartnerCode code) {
        String body = outbound.fetchText(urls.health(code));
        return Map.of("partner", code.name(), "bytes", body == null ? 0 : body.length());
    }

    public Map<String, Object> sync(PartnerCode code) {
        byte[] catalog = outbound.download(urls.catalog(code));
        return Map.of("partner", code.name(), "bytes", catalog == null ? 0 : catalog.length);
    }

    public String docsUrl(PartnerCode code) {
        return urls.docs(code);
    }
}
