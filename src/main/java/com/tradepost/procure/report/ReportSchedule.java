package com.tradepost.procure.report;

public record ReportSchedule(long id, long reportId, String recipient, String callbackUrl, String status) {
}
