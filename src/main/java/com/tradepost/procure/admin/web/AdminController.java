package com.tradepost.procure.admin.web;

import com.tradepost.procure.admin.PurgeTarget;
import com.tradepost.procure.admin.service.AuditService;
import com.tradepost.procure.admin.service.BundleService;
import com.tradepost.procure.admin.service.DiagnosticsService;
import com.tradepost.procure.admin.service.PurgeService;
import com.tradepost.procure.admin.service.SettingsService;
import com.tradepost.procure.admin.service.TaskService;
import com.tradepost.procure.admin.service.TemplateTestService;
import com.tradepost.procure.admin.web.AdminDtos.AuditFilter;
import com.tradepost.procure.admin.web.AdminDtos.BundleRequest;
import com.tradepost.procure.admin.web.AdminDtos.SettingsRequest;
import com.tradepost.procure.admin.web.AdminDtos.TemplateTestRequest;
import com.tradepost.procure.report.ReportEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuditService auditService;
    private final TaskService taskService;
    private final DiagnosticsService diagnostics;
    private final BundleService bundles;
    private final SettingsService settings;
    private final TemplateTestService templates;
    private final PurgeService purgeService;

    public AdminController(AuditService auditService, TaskService taskService, DiagnosticsService diagnostics,
                           BundleService bundles, SettingsService settings, TemplateTestService templates,
                           PurgeService purgeService) {
        this.auditService = auditService;
        this.taskService = taskService;
        this.diagnostics = diagnostics;
        this.bundles = bundles;
        this.settings = settings;
        this.templates = templates;
        this.purgeService = purgeService;
    }

    @GetMapping("/audit")
    public List<?> audit(@Valid AuditFilter filter) {
        return auditService.search(filter.actor(), filter.action());
    }

    @GetMapping(value = "/audit/export", produces = "text/plain")
    public String exportAudit() {
        return auditService.export();
    }

    @GetMapping("/stats")
    public List<Map<String, Object>> stats(@RequestParam ReportEntity entity, @RequestParam StatColumn by) {
        return auditService.stats(entity, by.value());
    }

    @PostMapping("/tasks/{name}/run")
    public Map<String, Object> runTask(@PathVariable String name, @RequestParam Cutoff cutoff) {
        return taskService.run(name, cutoff.value());
    }

    @GetMapping("/diagnostics/ping")
    public Map<String, Object> ping(@RequestParam String host) {
        return diagnostics.ping(host);
    }

    @PostMapping("/settings/bundles")
    public ResponseEntity<Map<String, Object>> storeBundle(@Valid @RequestBody BundleRequest request) {
        long id = bundles.store(request.name(), request.blobHex());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PostMapping("/settings/bundles/{id}/apply")
    public Map<String, Object> applyBundle(@PathVariable long id) {
        return bundles.apply(id);
    }

    @PutMapping("/settings/{key}")
    public Map<String, Object> putSetting(@PathVariable @Pattern(regexp = "^[a-z][a-z0-9.-]{2,40}$") String key,
                                          @Valid @RequestBody SettingsRequest request) {
        settings.put(key, request.value(), request.changedBy());
        return Map.of("key", key, "updated", true);
    }

    @PostMapping("/templates/test")
    public Map<String, Object> testTemplate(@Valid @RequestBody TemplateTestRequest request) {
        return Map.of("rendered", templates.test(request.template()));
    }

    @PostMapping("/purge")
    public Map<String, Object> purge(@RequestParam PurgeTarget target, @RequestParam int retentionDays)
            throws IOException {
        return purgeService.purge(target, retentionDays);
    }
}
