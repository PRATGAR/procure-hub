package com.tradepost.procure.pricing.service;

import com.tradepost.procure.common.EvalContexts;
import com.tradepost.procure.common.ExpressionService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("expression")
public class ExpressionStrategy implements PricingStrategy {

    private final ExpressionService expressions;

    public ExpressionStrategy(ExpressionService expressions) {
        this.expressions = expressions;
    }

    @Override
    public Object apply(SimulationRequested request) {
        return expressions.evaluate(request.expression(), EvalContexts.standard(Map.of()), request.facts());
    }
}
