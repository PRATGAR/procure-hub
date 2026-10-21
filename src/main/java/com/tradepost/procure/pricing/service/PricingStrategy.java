package com.tradepost.procure.pricing.service;

public interface PricingStrategy {

    Object apply(SimulationRequested request);
}
