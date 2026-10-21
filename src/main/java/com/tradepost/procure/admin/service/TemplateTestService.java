package com.tradepost.procure.admin.service;

import com.tradepost.procure.common.EvalContexts;
import com.tradepost.procure.common.ExpressionService;
import com.tradepost.procure.order.OrderFacts;
import org.springframework.stereotype.Service;

@Service
public class TemplateTestService {

    private final ExpressionService expressions;

    public TemplateTestService(ExpressionService expressions) {
        this.expressions = expressions;
    }

    public String test(String template) {
        OrderFacts sample = new OrderFacts("PO-0000", 125000, "EUR", "Sample dock, 70173 Stuttgart",
                "Sample Supplier GmbH", "DRAFT");
        return expressions.render(template, EvalContexts.simple(), sample);
    }
}
