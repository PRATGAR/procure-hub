package com.tradepost.procure.document.web;

import com.tradepost.procure.common.CommandResult;
import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.document.ConvertProfile;
import com.tradepost.procure.document.OwnerType;
import com.tradepost.procure.document.service.DocumentService;
import com.tradepost.procure.document.web.DocumentDtos.RegisterRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
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
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;
    private final PageWriter writer;

    public DocumentController(DocumentService service, PageWriter writer) {
        this.service = service;
        this.writer = writer;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        long id = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "REGISTERED"));
    }

    @GetMapping("/{id}/download")
    public byte[] download(@PathVariable long id, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return service.download(id);
    }

    @GetMapping("/{id}/archive")
    public byte[] archive(@PathVariable long id, HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        return service.archive(id);
    }

    @PostMapping("/{id}/convert")
    public Map<String, Object> convert(@PathVariable long id) {
        CommandResult result = service.convert(id);
        return Map.of("documentId", id, "exitCode", result.exitCode(), "output", result.output());
    }

    @GetMapping("/{id}/card")
    public void card(@PathVariable long id, HttpServletResponse response) throws IOException {
        writer.html(response, writer.page("Document", service.card(id)));
    }

    @GetMapping("/by-name")
    public byte[] byName(@RequestParam String name, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return service.byName(name);
    }

    @PostMapping(value = "/{id}/metadata", consumes = {"application/xml", "text/xml"})
    public Map<String, String> metadata(@PathVariable long id, @RequestBody String xml)
            throws ParserConfigurationException, SAXException, IOException {
        return service.metadata(id, xml);
    }

    @PostMapping("/{id}/publish")
    public Map<String, Object> publish(@PathVariable long id, @RequestParam(defaultValue = "pdf") String format,
                                       @RequestParam(defaultValue = "out") String prefix) {
        CommandResult result = service.publish(id, format, prefix);
        return Map.of("documentId", id, "format", format, "exitCode", result.exitCode());
    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> attach(@RequestParam MultipartFile file,
                                                      @RequestParam OwnerType ownerType,
                                                      @RequestParam long ownerId) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.attach(file, ownerType, ownerId));
    }

    @PostMapping("/{id}/text-extract")
    public Map<String, Object> textExtract(@PathVariable long id,
                                           @RequestParam(defaultValue = "TEXT") ConvertProfile profile) {
        CommandResult result = service.textExtract(id, profile);
        return Map.of("documentId", id, "profile", profile.name(), "exitCode", result.exitCode());
    }

    @PostMapping("/{id}/export-archive")
    public Map<String, Object> exportArchive(@PathVariable long id, @RequestParam(defaultValue = "1") int copies)
            throws IOException {
        return service.exportArchive(id, copies);
    }
}
