package com.tradepost.procure.report.service;

import com.tradepost.procure.common.Html;
import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.report.SavedReport;
import org.springframework.stereotype.Component;

@Component
public class ReportPages {

    private final PageWriter writer;

    public ReportPages(PageWriter writer) {
        this.writer = writer;
    }

    public String preview(SavedReport report) {
        String body = "<h1>" + Html.escape(report.name()) + "</h1>"
                + "<p class=\"entity\">" + report.entity() + "</p>"
                + "<div class=\"description\">" + report.description() + "</div>";
        return writer.page("Report preview", body);
    }

    public String print(SavedReport report) {
        String body = "<h1>" + Html.escape(report.name()) + "</h1>"
                + "<p class=\"entity\">" + report.entity() + "</p>"
                + "<div class=\"description\">" + Html.escape(report.description()) + "</div>";
        return writer.page("Report", body);
    }
}
