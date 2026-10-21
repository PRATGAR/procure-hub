package com.tradepost.procure.pricing.service;

import com.tradepost.procure.common.InvalidRequestException;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SimulationListener {

    private final Map<String, PricingStrategy> strategies;

    public SimulationListener(Map<String, PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    @EventListener
    public void onSimulationRequested(SimulationRequested event) {
        PricingStrategy strategy = strategies.get(event.strategy());
        if (strategy == null) {
            throw new InvalidRequestException("unknown pricing strategy");
        }
        event.result().set(strategy.apply(event));
    }
}
