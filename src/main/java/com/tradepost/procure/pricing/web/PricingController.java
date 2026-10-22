package com.tradepost.procure.pricing.web;

import com.tradepost.procure.common.CurrencyCode;
import com.tradepost.procure.pricing.RuleScope;
import com.tradepost.procure.pricing.service.PricingService;
import com.tradepost.procure.pricing.service.RuleImportService;
import com.tradepost.procure.pricing.web.PricingDtos.CreateRuleRequest;
import com.tradepost.procure.pricing.web.PricingDtos.Created;
import com.tradepost.procure.pricing.web.PricingDtos.Explanation;
import com.tradepost.procure.pricing.web.PricingDtos.FxRate;
import com.tradepost.procure.pricing.web.PricingDtos.ImportResult;
import com.tradepost.procure.pricing.web.PricingDtos.PreviewRequest;
import com.tradepost.procure.pricing.web.PricingDtos.PreviewResult;
import com.tradepost.procure.pricing.web.PricingDtos.QuoteRequest;
import com.tradepost.procure.pricing.web.PricingDtos.QuoteResult;
import com.tradepost.procure.pricing.web.PricingDtos.RuleRate;
import com.tradepost.procure.pricing.web.PricingDtos.SimulateRequest;
import com.tradepost.procure.pricing.web.PricingDtos.SimulationResult;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService service;
    private final RuleImportService importer;

    public PricingController(PricingService service, RuleImportService importer) {
        this.service = service;
        this.importer = importer;
    }

    @PostMapping("/rules")
    public ResponseEntity<Created> createRule(@Valid @RequestBody CreateRuleRequest request) {
        long id = service.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(id));
    }

    @GetMapping("/rules")
    public List<Map<String, Object>> rules(@RequestParam(required = false) RuleScope scope) {
        return service.rules(scope);
    }

    @GetMapping("/rules/{id}/rate")
    public RuleRate ruleRate(@PathVariable long id) {
        return service.ruleRate(id);
    }

    @PostMapping("/quotes")
    public ResponseEntity<QuoteResult> quote(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.quote(request));
    }

    @GetMapping("/quotes/{id}/explain")
    public Explanation explain(@PathVariable long id) {
        return new Explanation(id, service.explain(id));
    }

    @PostMapping("/rules/preview")
    public PreviewResult preview(@Valid @RequestBody PreviewRequest request) {
        return new PreviewResult(String.valueOf(service.preview(request)));
    }

    @GetMapping("/fx/{currency}")
    public FxRate fx(@PathVariable CurrencyCode currency) {
        return new FxRate(currency.name(), service.fxRate(currency.name()));
    }

    @PostMapping("/simulate")
    public SimulationResult simulate(@Valid @RequestBody SimulateRequest request) {
        return new SimulationResult(request.strategy(), String.valueOf(service.simulate(request)));
    }

    @PostMapping(value = "/rules/import", consumes = {"application/x-yaml", "text/yaml", "text/plain"})
    public ImportResult importRules(@RequestBody String body) {
        return new ImportResult(importer.importRules(body));
    }
}
