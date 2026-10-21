package com.tradepost.procure.report.service;

public record ReportScheduled(long scheduleId, long reportId, String recipient, String callbackUrl) {
}
