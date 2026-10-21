package com.tradepost.procure.pricing.service;

import com.tradepost.procure.pricing.QuoteFacts;
import java.util.concurrent.atomic.AtomicReference;

public record SimulationRequested(String strategy, String expression, QuoteFacts facts,
                                  AtomicReference<Object> result) {
}
