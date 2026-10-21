package com.tradepost.procure.common;

import java.util.Map;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public final class EvalContexts {

    private EvalContexts() {
    }

    public static EvaluationContext standard(Map<String, Object> variables) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        variables.forEach(context::setVariable);
        return context;
    }

    public static EvaluationContext simple() {
        return SimpleEvaluationContext.forReadOnlyDataBinding().build();
    }
}
