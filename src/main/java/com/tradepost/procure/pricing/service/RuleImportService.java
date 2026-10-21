package com.tradepost.procure.pricing.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.YamlFactories;
import com.tradepost.procure.pricing.RuleScope;
import com.tradepost.procure.pricing.repo.PricingRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
public class RuleImportService {

    private final PricingRepository prices;
    private final AuditWriter audit;

    public RuleImportService(PricingRepository prices, AuditWriter audit) {
        this.prices = prices;
        this.audit = audit;
    }

    public int importRules(String body) {
        Yaml yaml = YamlFactories.ruleFiles();
        Object parsed = yaml.load(body);
        if (!(parsed instanceof List<?> entries)) {
            throw new InvalidRequestException("a list of rules is expected");
        }
        int imported = 0;
        for (Object entry : entries) {
            if (!(entry instanceof Map<?, ?> rule)) {
                continue;
            }
            String name = String.valueOf(rule.get("name"));
            RuleScope scope = RuleScope.valueOf(String.valueOf(rule.get("scope")));
            String expression = String.valueOf(rule.get("discountExpr"));
            String currency = String.valueOf(rule.get("currency"));
            Object explanation = rule.get("explanation");
            prices.insertRule(name, scope.name(), expression, explanation == null ? null : explanation.toString(), currency);
            imported++;
        }
        audit.append(Actors.current(), "pricing.rules.imported", "count " + imported);
        return imported;
    }
}
