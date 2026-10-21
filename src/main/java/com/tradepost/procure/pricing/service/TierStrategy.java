package com.tradepost.procure.pricing.service;

import com.tradepost.procure.pricing.repo.PricingRepository;
import org.springframework.stereotype.Component;

@Component("tier")
public class TierStrategy implements PricingStrategy {

    private final PricingRepository prices;

    public TierStrategy(PricingRepository prices) {
        this.prices = prices;
    }

    @Override
    public Object apply(SimulationRequested request) {
        return prices.tierBpFor(request.facts().getQty());
    }
}
