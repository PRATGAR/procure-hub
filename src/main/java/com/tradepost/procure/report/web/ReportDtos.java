package com.tradepost.procure.report.web;

import com.tradepost.procure.report.InvoiceState;
import com.tradepost.procure.report.ReportEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ReportDtos {

    private ReportDtos() {
    }

    public record CreateReportRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull ReportEntity entity,
            @NotBlank @Size(max = 400) String whereClause,
            @Size(max = 600) String description) {
    }

    public record FilterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull InvoiceState status,
            @Min(0) long minTotalCents,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency) {
    }

    public record ScheduleRequest(
            @NotBlank @Email @Size(max = 160) String recipient,
            @Size(max = 300) String callbackUrl) {
    }

    public record ExportPdfRequest(@Size(max = 120) String title) {
    }
}
