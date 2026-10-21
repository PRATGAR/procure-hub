package com.tradepost.procure.report;

public record ReportRun(long id, long reportId, String fileName, int rowCount, String status) {
}
