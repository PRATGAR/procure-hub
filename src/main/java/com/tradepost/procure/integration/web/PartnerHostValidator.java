package com.tradepost.procure.integration.web;

import com.tradepost.procure.common.HostAllowlist;
import com.tradepost.procure.config.TradepostProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PartnerHostValidator implements ConstraintValidator<PartnerHost, String> {

    private final TradepostProperties properties;

    public PartnerHostValidator(TradepostProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return HostAllowlist.allows(value, properties.partnerHosts());
    }
}
