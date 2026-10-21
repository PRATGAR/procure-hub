package com.tradepost.procure.integration.web;

import com.tradepost.procure.integration.PartnerCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class IntegrationDtos {

    private IntegrationDtos() {
    }

    public record CreatePartnerRequest(
            @NotNull PartnerCode code,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @PartnerHost String baseUrl,
            @Size(max = 300) String portalUrl,
            @Pattern(regexp = "^(MD5|SHA-256)$") String sigAlg) {
    }

    public record RegisterWebhookRequest(
            @Min(1) long partnerId,
            @NotBlank @Size(max = 300) String url,
            @NotBlank @Pattern(regexp = "^[a-z]+(\\.[a-z]+){0,2}$") String eventType) {
    }

    public record PartnerEventRequest(
            @NotBlank @Pattern(regexp = "^[a-z]+(\\.[a-z]+){0,2}$") String eventType,
            @Size(max = 2000) String body) {
    }

    public record InboundRequest(
            @NotBlank @Size(max = 40) String eventType,
            @NotBlank @Pattern(regexp = "^[0-9a-f]{2,7000}$") String payloadHex) {
    }
}
