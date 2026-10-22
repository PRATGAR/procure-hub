package com.tradepost.procure.report.web;

import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.report.ReportRun;
import com.tradepost.procure.report.SavedReport;
import com.tradepost.procure.report.SummaryDimension;
import com.tradepost.procure.report.service.ReportService;
import com.tradepost.procure.report.web.ReportDtos.CreateReportRequest;
import com.tradepost.procure.report.web.ReportDtos.Created;
import com.tradepost.procure.report.web.ReportDtos.Dispatched;
import com.tradepost.procure.report.web.ReportDtos.ExportPdfRequest;
import com.tradepost.procure.report.web.ReportDtos.FilterRequest;
import com.tradepost.procure.report.web.ReportDtos.FilterRun;
import com.tradepost.procure.report.web.ReportDtos.PdfExported;
import com.tradepost.procure.report.web.ReportDtos.ReportQuery;
import com.tradepost.procure.report.web.ReportDtos.RunQueued;
import com.tradepost.procure.report.web.ReportDtos.ScheduleCreated;
import com.tradepost.procure.report.web.ReportDtos.ScheduleRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;
    private final PageWriter writer;

    public ReportController(ReportService service, PageWriter writer) {
        this.service = service;
        this.writer = writer;
    }

    @PostMapping
    public ResponseEntity<Created> create(@Valid @RequestBody CreateReportRequest request) {
        long id = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(id));
    }

    @GetMapping
    public List<SavedReport> search(@Valid ReportQuery query) {
        return service.search(query.q());
    }

    @GetMapping("/{id}/similar")
    public List<SavedReport> similar(@PathVariable long id) {
        return service.similar(id);
    }

    @PostMapping("/{id}/run")
    public RunQueued run(@PathVariable long id) {
        return service.run(id);
    }

    @PostMapping("/filters")
    public ResponseEntity<Created> createFilter(@Valid @RequestBody FilterRequest request) {
        long id = service.createFilter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(id));
    }

    @GetMapping("/filters/{id}/run")
    public FilterRun runFilter(@PathVariable long id) {
        return service.runFilter(id);
    }

    @GetMapping("/{id}/preview")
    public void preview(@PathVariable long id, HttpServletResponse response) throws IOException {
        writer.html(response, service.preview(id));
    }

    @GetMapping("/runs/{runId}/download")
    public byte[] download(@PathVariable long runId, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return service.download(runId);
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<ScheduleCreated> schedule(@PathVariable long id,
                                                    @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.schedule(id, request));
    }

    @PostMapping("/schedules/{scheduleId}/dispatch")
    public Dispatched dispatch(@PathVariable long scheduleId) {
        return service.dispatch(scheduleId);
    }

    @PostMapping("/{id}/export-pdf")
    public PdfExported exportPdf(@PathVariable long id, @Valid @RequestBody ExportPdfRequest request) {
        return service.exportPdf(id, request);
    }

    @GetMapping("/summary/{dimension}")
    public List<Map<String, Object>> summary(@PathVariable SummaryDimension dimension) {
        return service.summary(dimension);
    }

    @GetMapping("/exports")
    public byte[] exportByName(@RequestParam String name, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return service.exportByName(name);
    }

    @GetMapping("/runs")
    public List<ReportRun> runs(@Valid PageParams page) {
        return service.runs(page.limit());
    }
}
