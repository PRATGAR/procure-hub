package com.tradepost.procure.pricing.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.EvalContexts;
import com.tradepost.procure.common.ExpressionService;
import com.tradepost.procure.common.FxRates;
import com.tradepost.procure.pricing.PriceRule;
import com.tradepost.procure.pricing.Quote;
import com.tradepost.procure.pricing.QuoteFacts;
import com.tradepost.procure.pricing.RuleScope;
import com.tradepost.procure.pricing.repo.PricingRepository;
import com.tradepost.procure.pricing.web.PricingDtos.CreateRuleRequest;
import com.tradepost.procure.pricing.web.PricingDtos.PreviewRequest;
import com.tradepost.procure.pricing.web.PricingDtos.QuoteRequest;
import com.tradepost.procure.pricing.web.PricingDtos.QuoteResult;
import com.tradepost.procure.pricing.web.PricingDtos.RuleRate;
import com.tradepost.procure.pricing.web.PricingDtos.SimulateRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private final PricingRepository prices;
    private final ExpressionService expressions;
    private final RuleEngine engine;
    private final FxRates fx;
    private final ApplicationEventPublisher events;
    private final AuditWriter audit;

    public PricingService(PricingRepository prices, ExpressionService expressions, RuleEngine engine, FxRates fx,
                          ApplicationEventPublisher events, AuditWriter audit) {
        this.engine = engine;
        this.prices = prices;
        this.expressions = expressions;
        this.fx = fx;
        this.events = events;
        this.audit = audit;
    }

    public long createRule(CreateRuleRequest request) {
        long id = prices.insertRule(request.name().trim(), request.scope().name(), request.discountExpr(),
                request.explanation(), request.currency().trim());
        audit.append(Actors.current(), "pricing.rule.created", "rule " + id);
        return id;
    }

    public List<Map<String, Object>> rules(RuleScope scope) {
        if (scope == null) {
            return prices.allRules();
        }
        return prices.rulesByScope(scope);
    }

    public QuoteResult quote(QuoteRequest request) {
        long unitCents = prices.unitPrice(request.sku());
        int tierBp = prices.tierBp(request.qty());
        int units = request.qty() * request.packSize();
        int volumeBp = prices.volumeBp(units);
        long gross = unitCents * request.qty();
        long bestBp = Math.max(tierBp, volumeBp);
        Long appliedRule = null;
        for (PriceRule rule : prices.activeRules(RuleScope.GLOBAL)) {
            QuoteFacts facts = new QuoteFacts(request.sku(), request.qty(), unitCents, gross,
                    request.currency().name());
            try {
                Object discount = engine.discountFor(rule, facts);
                if (discount instanceof Number number) {
                    long bp = Math.round(number.doubleValue() * 10_000);
                    if (bp > bestBp) {
                        bestBp = bp;
                        appliedRule = rule.id();
                    }
                }
            } catch (ExpressionException e) {
                log.warn("pricing rule {} could not be evaluated", rule.id());
            }
        }
        long total = gross * (10_000 - bestBp) / 10_000;
        long id = prices.insertQuote(request.sku(), request.qty(), request.currency().name(), unitCents, total, appliedRule);
        return new QuoteResult(id, unitCents, total, bestBp);
    }

    public String explain(long quoteId) {
        Quote quote = prices.findQuote(quoteId);
        if (quote.ruleId() == null) {
            return "No pricing rule was applied to this quote.";
        }
        PriceRule rule = prices.findRule(quote.ruleId());
        if (rule.explanation() == null) {
            return rule.name();
        }
        QuoteFacts facts = new QuoteFacts(quote.itemSku(), quote.qty(), quote.unitCents(), quote.totalCents(),
                quote.currency());
        return expressions.render(rule.explanation(), EvalContexts.simple(), facts);
    }

    public Object preview(PreviewRequest request) {
        QuoteFacts facts = new QuoteFacts("PREVIEW", request.qty(), request.unitCents(),
                request.unitCents() * request.qty(), "EUR");
        return expressions.evaluate(request.expression(), EvalContexts.standard(Map.of()), facts);
    }

    public long fxRate(String currency) {
        return fx.rateMicro(currency);
    }

    public RuleRate ruleRate(long ruleId) {
        PriceRule rule = prices.findRule(ruleId);
        return new RuleRate(ruleId, rule.currency(), fx.rateMicro(rule.currency()));
    }

    public Object simulate(SimulateRequest request) {
        QuoteFacts facts = new QuoteFacts("SIMULATION", request.qty(), request.unitCents(),
                request.unitCents() * request.qty(), "EUR");
        AtomicReference<Object> result = new AtomicReference<>();
        events.publishEvent(new SimulationRequested(request.strategy(), request.expression(), facts, result));
        return result.get();
    }
}
