package com.tradepost.procure.report.service;

import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.report.ReportSchedule;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ScheduleDispatcher {

    private final OutboundHttp outbound;

    public ScheduleDispatcher(OutboundHttp outbound) {
        this.outbound = outbound;
    }

    public boolean dispatch(ReportSchedule schedule) {
        if (schedule.callbackUrl() == null || schedule.callbackUrl().isBlank()) {
            return false;
        }
        outbound.deliver(schedule.callbackUrl(),
                Map.of("reportId", schedule.reportId(), "scheduleId", schedule.id()));
        return true;
    }
}
