package com.tradepost.procure.config;

import java.security.SecureRandom;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RandomSources {

    @Bean("invitationPins")
    public Random invitationPins(TradepostProperties properties) {
        return new Random(properties.security().pinSeed());
    }

    @Bean("invitationTokens")
    public Random invitationTokens() {
        return new SecureRandom();
    }

    @Bean("approvalCodes")
    public Random approvalCodes() {
        return new Random(System.currentTimeMillis());
    }

    @Bean("webhookSecrets")
    public Random webhookSecrets() {
        return new Random();
    }
}
