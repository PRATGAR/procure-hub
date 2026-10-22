package com.tradepost.procure.report.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.Aggregates;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.CommandResult;
import com.tradepost.procure.common.FileNames;
import com.tradepost.procure.common.FileStore;
import com.tradepost.procure.common.ProcessRunner;
import com.tradepost.procure.common.ShellCommands;
import com.tradepost.procure.report.ReportEntity;
import com.tradepost.procure.report.ReportRun;
import com.tradepost.procure.report.ReportSchedule;
import com.tradepost.procure.report.SavedFilter;
import com.tradepost.procure.report.SavedReport;
import com.tradepost.procure.report.SummaryDimension;
import com.tradepost.procure.report.repo.ReportRepository;
import com.tradepost.procure.report.web.ReportDtos.CreateReportRequest;
import com.tradepost.procure.report.web.ReportDtos.Dispatched;
import com.tradepost.procure.report.web.ReportDtos.ExportPdfRequest;
import com.tradepost.procure.report.web.ReportDtos.FilterRequest;
import com.tradepost.procure.report.web.ReportDtos.FilterRun;
import com.tradepost.procure.report.web.ReportDtos.PdfExported;
import com.tradepost.procure.report.web.ReportDtos.RunQueued;
import com.tradepost.procure.report.web.ReportDtos.ScheduleCreated;
import com.tradepost.procure.report.web.ReportDtos.ScheduleRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final Duration PDF_TIMEOUT = Duration.ofSeconds(30);

    private final ReportRepository reports;
    private final Aggregates aggregates;
    private final FileStore store;
    private final ProcessRunner runner;
    private final ReportPages pages;
    private final ScheduleDispatcher dispatcher;
    private final AuditWriter audit;

    public ReportService(ReportRepository reports, Aggregates aggregates, FileStore store, ProcessRunner runner,
                         ReportPages pages, ScheduleDispatcher dispatcher, AuditWriter audit) {
        this.reports = reports;
        this.aggregates = aggregates;
        this.store = store;
        this.runner = runner;
        this.pages = pages;
        this.dispatcher = dispatcher;
        this.audit = audit;
    }

    public long create(CreateReportRequest request) {
        long id = reports.insertReport(request.name().trim(), request.entity().name(), request.whereClause(),
                request.description(), Actors.current());
        audit.append(Actors.current(), "report.created", "report " + id);
        return id;
    }

    public List<SavedReport> search(String q) {
        return reports.search(q);
    }

    public List<SavedReport> similar(long id) {
        SavedReport report = reports.findReport(id);
        return reports.byNameLike(report.name());
    }

    public RunQueued run(long id) {
        SavedReport report = reports.findReport(id);
        ReportEntity entity = ReportEntity.valueOf(report.entity());
        int rows = reports.runReport(entity.table(), report.whereClause());
        long runId = reports.insertRun(id, "pending", rows);
        String fileName = "report-" + id + "-" + runId + ".csv";
        reports.renameRun(runId, fileName);
        return new RunQueued(runId, rows, "QUEUED", fileName);
    }

    public long createFilter(FilterRequest request) {
        String clause = "status = '" + request.status().name() + "' AND total_cents >= " + request.minTotalCents()
                + " AND currency = '" + request.currency() + "'";
        return reports.insertFilter(request.name().trim(), ReportEntity.INVOICE.name(), clause);
    }

    public FilterRun runFilter(long id) {
        SavedFilter filter = reports.findFilter(id);
        List<?> rows = reports.runFilter(ReportEntity.valueOf(filter.entity()).table(), filter.whereClause());
        return new FilterRun(id, rows.size());
    }

    public byte[] download(long runId) throws IOException {
        ReportRun run = reports.findRun(runId);
        return store.read(FileStore.EXPORTS, run.fileName());
    }

    public String preview(long id) {
        return pages.preview(reports.findReport(id));
    }

    public ScheduleCreated schedule(long id, ScheduleRequest request) {
        reports.findReport(id);
        long scheduleId = reports.insertSchedule(id, request.recipient(), request.callbackUrl());
        audit.append(Actors.current(), "report.scheduled", "report " + id);
        return new ScheduleCreated(scheduleId, "ACTIVE");
    }

    public Dispatched dispatch(long scheduleId) {
        ReportSchedule schedule = reports.findSchedule(scheduleId);
        boolean delivered = dispatcher.dispatch(schedule);
        audit.append(Actors.current(), "report.dispatched", "schedule " + scheduleId);
        return new Dispatched(scheduleId, delivered);
    }

    public PdfExported exportPdf(long id, ExportPdfRequest request) {
        SavedReport report = reports.findReport(id);
        log.info("pdf export for report " + id + " by " + Actors.current());
        String title = request.title() == null || request.title().isBlank() ? report.name() : request.title();
        CommandResult result = runner.run(ShellCommands.reportPdf(id, title), PDF_TIMEOUT);
        return new PdfExported(id, result.exitCode());
    }

    public List<Map<String, Object>> summary(SummaryDimension dimension) {
        return aggregates.countBy(dimension.table(), dimension.column());
    }

    public byte[] exportByName(String name) throws IOException {
        return store.read(FileStore.EXPORTS, FileNames.leaf(name));
    }

    public List<ReportRun> runs(int limit) {
        return reports.recentRuns(limit);
    }
}
