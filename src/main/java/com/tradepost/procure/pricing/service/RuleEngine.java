package com.tradepost.procure.pricing.service;

import com.tradepost.procure.common.EvalContexts;
import com.tradepost.procure.common.ExpressionService;
import com.tradepost.procure.pricing.PriceRule;
import com.tradepost.procure.pricing.QuoteFacts;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RuleEngine {

    private final ExpressionService expressions;

    public RuleEngine(ExpressionService expressions) {
        this.expressions = expressions;
    }

    public Object discountFor(PriceRule rule, QuoteFacts facts) {
        return expressions.evaluate(rule.discountExpr(), EvalContexts.standard(Map.of()), facts);
    }
}
