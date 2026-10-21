package com.tradepost.procure.admin.service;

import com.tradepost.procure.admin.AuditAction;
import com.tradepost.procure.admin.repo.SettingsRepository;
import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.Html;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private static final String BANNER_KEY = "portal.banner";

    private final SettingsRepository settings;
    private final AuditWriter audit;

    public SettingsService(SettingsRepository settings, AuditWriter audit) {
        this.settings = settings;
        this.audit = audit;
    }

    public void put(String key, String value, String changedBy) {
        String stored = BANNER_KEY.equals(key) ? Html.escape(value) : value;
        settings.upsert(key, stored, changedBy);
        String actor = changedBy == null || changedBy.isBlank() ? Actors.current() : changedBy;
        audit.append(actor, AuditAction.SETTING.code(), key);
    }

    public Optional<String> get(String key) {
        return settings.find(key);
    }

    public String banner() {
        return settings.find(BANNER_KEY).orElse("");
    }
}
