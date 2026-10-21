package com.tradepost.procure.pricing.web;

import com.tradepost.procure.common.CurrencyCode;
import com.tradepost.procure.pricing.RuleScope;
import com.tradepost.procure.pricing.service.PricingService;
import com.tradepost.procure.pricing.service.RuleImportService;
import com.tradepost.procure.pricing.web.PricingDtos.CreateRuleRequest;
import com.tradepost.procure.pricing.web.PricingDtos.PreviewRequest;
import com.tradepost.procure.pricing.web.PricingDtos.QuoteRequest;
import com.tradepost.procure.pricing.web.PricingDtos.SimulateRequest;
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
    public ResponseEntity<Map<String, Object>> createRule(@Valid @RequestBody CreateRuleRequest request) {
        long id = service.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @GetMapping("/rules")
    public List<Map<String, Object>> rules(@RequestParam(required = false) RuleScope scope) {
        return service.rules(scope);
    }

    @PostMapping("/quotes")
    public ResponseEntity<Map<String, Object>> quote(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.quote(request));
    }

    @GetMapping("/quotes/{id}/explain")
    public Map<String, Object> explain(@PathVariable long id) {
        return Map.of("quoteId", id, "explanation", service.explain(id));
    }

    @PostMapping("/rules/preview")
    public Map<String, Object> preview(@Valid @RequestBody PreviewRequest request) {
        return Map.of("result", String.valueOf(service.preview(request)));
    }

    @GetMapping("/fx/{currency}")
    public Map<String, Object> fx(@PathVariable CurrencyCode currency) {
        return Map.of("currency", currency.name(), "rateMicro", service.fxRate(currency.name()));
    }

    @PostMapping("/simulate")
    public Map<String, Object> simulate(@Valid @RequestBody SimulateRequest request) {
        return Map.of("strategy", request.strategy(), "result", String.valueOf(service.simulate(request)));
    }

    @PostMapping(value = "/rules/import", consumes = {"application/x-yaml", "text/yaml", "text/plain"})
    public Map<String, Object> importRules(@RequestBody String body) {
        return Map.of("imported", importer.importRules(body));
    }
}
