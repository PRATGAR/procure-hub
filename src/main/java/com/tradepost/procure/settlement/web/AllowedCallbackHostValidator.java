package com.tradepost.procure.settlement.web;

import com.tradepost.procure.common.HostAllowlist;
import com.tradepost.procure.config.TradepostProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedCallbackHostValidator implements ConstraintValidator<AllowedCallbackHost, String> {

    private final TradepostProperties properties;

    public AllowedCallbackHostValidator(TradepostProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return HostAllowlist.allows(value, properties.approvedCallbackHosts());
    }
}
