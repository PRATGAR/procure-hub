package com.tradepost.procure.common;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

@Component
public class ExpressionService {

    private final ExpressionParser parser = new SpelExpressionParser();

    public Object evaluate(String expression, EvaluationContext context, Object root) {
        Expression compiled = parser.parseExpression(expression);
        return compiled.getValue(context, root);
    }

    public String render(String template, EvaluationContext context, Object root) {
        Expression compiled = parser.parseExpression(template, new TemplateParserContext());
        return compiled.getValue(context, root, String.class);
    }
}
