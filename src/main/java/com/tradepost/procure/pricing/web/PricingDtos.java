package com.tradepost.procure.pricing.web;

import com.tradepost.procure.common.CurrencyCode;
import com.tradepost.procure.pricing.RuleScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class PricingDtos {

    private PricingDtos() {
    }

    public record CreateRuleRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull RuleScope scope,
            @NotBlank @Size(max = 300) String discountExpr,
            @Size(max = 300) String explanation,
            @NotBlank @Size(max = 12) String currency) {
    }

    public record QuoteRequest(
            @NotBlank @Pattern(regexp = "^[A-Z0-9-]{4,24}$") String sku,
            @Min(1) @Max(100000) int qty,
            @Min(1) int packSize,
            @NotNull CurrencyCode currency) {
    }

    public record PreviewRequest(
            @NotBlank @Size(max = 300) String expression,
            @Min(1) @Max(100000) int qty,
            @Min(1) long unitCents) {
    }

    public record SimulateRequest(
            @NotBlank @Pattern(regexp = "^[a-z]{3,12}$") String strategy,
            @Size(max = 300) String expression,
            @Min(1) @Max(100000) int qty,
            @Min(1) long unitCents) {
    }
}
