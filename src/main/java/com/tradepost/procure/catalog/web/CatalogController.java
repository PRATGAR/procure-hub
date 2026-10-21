package com.tradepost.procure.catalog.web;

import com.tradepost.procure.catalog.Item;
import com.tradepost.procure.catalog.Media;
import com.tradepost.procure.catalog.service.CatalogImportService;
import com.tradepost.procure.catalog.service.CatalogService;
import com.tradepost.procure.catalog.web.CatalogDtos.CreateItemRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.FetchRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.ItemDetail;
import com.tradepost.procure.catalog.web.CatalogDtos.MediaRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.ThumbnailRequest;
import com.tradepost.procure.common.PageWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService service;
    private final CatalogImportService imports;
    private final PageWriter writer;

    public CatalogController(CatalogService service, CatalogImportService imports, PageWriter writer) {
        this.service = service;
        this.imports = imports;
        this.writer = writer;
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateItemRequest request) {
        long id = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "DRAFT"));
    }

    @GetMapping("/items")
    public List<Map<String, Object>> search(@RequestParam(defaultValue = "") String q,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false) String dir,
                                            @RequestParam(defaultValue = "25") int limit) {
        return service.search(q, sort, dir, limit);
    }

    @GetMapping("/items/{id}")
    public ItemDetail detail(@PathVariable long id) {
        return service.detail(id);
    }

    @GetMapping("/categories/{categoryId}/items")
    public List<Item> inCategory(@PathVariable long categoryId, @RequestParam(required = false) String dir) {
        return service.inCategory(categoryId, dir);
    }

    @PostMapping("/items/{id}/media")
    public ResponseEntity<Map<String, Object>> registerMedia(@PathVariable long id,
                                                             @Valid @RequestBody MediaRequest request) {
        long mediaId = service.registerMedia(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", mediaId));
    }

    @GetMapping("/items/{id}/media/{mediaId}")
    public byte[] media(@PathVariable long id, @PathVariable long mediaId, HttpServletResponse response)
            throws IOException {
        Media media = service.media(id, mediaId);
        byte[] content = service.mediaContent(media);
        response.setContentType(media.contentType());
        response.setHeader("Content-Disposition", "inline; filename=\"" + media.storedName() + "\"");
        return content;
    }

    @PostMapping("/items/{id}/thumbnails")
    public ResponseEntity<Map<String, Object>> registerThumbnail(@PathVariable long id,
                                                                 @Valid @RequestBody ThumbnailRequest request) {
        long thumbId = service.registerThumbnail(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", thumbId));
    }

    @GetMapping("/items/{id}/thumbnails/{thumbId}")
    public byte[] thumbnail(@PathVariable long id, @PathVariable long thumbId, HttpServletResponse response)
            throws IOException {
        response.setContentType("image/png");
        return service.thumbnailContent(id, thumbId);
    }

    @PostMapping("/items/{id}/media/fetch")
    public Map<String, Object> fetchMedia(@PathVariable long id, @Valid @RequestBody FetchRequest request) {
        return service.fetchRemoteMedia(id, request.sourceUrl());
    }

    @PostMapping(value = "/import", consumes = {"application/xml", "text/xml", "text/csv", "text/plain"})
    public Map<String, Object> importFeed(@RequestParam(defaultValue = "xml") String format,
                                          @RequestBody String payload) {
        int imported = imports.start(format, payload);
        return Map.of("format", format, "imported", imported);
    }

    @GetMapping("/items/{id}/card")
    public void card(@PathVariable long id, HttpServletResponse response) throws IOException {
        writer.html(response, writer.page("Item", service.card(id)));
    }

    @GetMapping("/items/{id}/spec/summary")
    public Map<String, Object> specSummary(@PathVariable long id, @RequestParam String field) {
        return Map.of("field", field, "value", service.specSummary(id, field));
    }
}
