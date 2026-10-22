package com.tradepost.procure.admin.service;

import com.tradepost.procure.admin.StatPreset;
import com.tradepost.procure.admin.repo.SettingsRepository;
import com.tradepost.procure.admin.web.AdminDtos.StatPresetRequest;
import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.Aggregates;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.report.ReportEntity;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StatPresetService {

    private final SettingsRepository settings;
    private final Aggregates aggregates;
    private final AuditWriter audit;

    public StatPresetService(SettingsRepository settings, Aggregates aggregates, AuditWriter audit) {
        this.settings = settings;
        this.aggregates = aggregates;
        this.audit = audit;
    }

    public long save(StatPresetRequest request) {
        long id = settings.insertPreset(request.name().trim(), request.entity().name(), request.column().trim());
        audit.append(Actors.current(), "admin.preset.saved", "preset " + id);
        return id;
    }

    public List<Map<String, Object>> run(long id) {
        StatPreset preset = settings.findPreset(id);
        ReportEntity entity = ReportEntity.valueOf(preset.entity());
        return aggregates.countBy(entity.table(), preset.columnExpr());
    }
}
