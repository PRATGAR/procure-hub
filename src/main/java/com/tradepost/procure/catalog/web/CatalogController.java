package com.tradepost.procure.catalog.web;

import com.tradepost.procure.catalog.Item;
import com.tradepost.procure.catalog.Media;
import com.tradepost.procure.catalog.service.CatalogImportService;
import com.tradepost.procure.catalog.service.CatalogService;
import com.tradepost.procure.catalog.web.CatalogDtos.CreateItemRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.Created;
import com.tradepost.procure.catalog.web.CatalogDtos.EntityId;
import com.tradepost.procure.catalog.web.CatalogDtos.FeedProcessed;
import com.tradepost.procure.catalog.web.CatalogDtos.FeedStaged;
import com.tradepost.procure.catalog.web.CatalogDtos.FetchRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.FetchResult;
import com.tradepost.procure.catalog.web.CatalogDtos.ItemDetail;
import com.tradepost.procure.catalog.web.CatalogDtos.LabelText;
import com.tradepost.procure.catalog.web.CatalogDtos.MediaRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.PrefsRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.PrefsSaved;
import com.tradepost.procure.catalog.web.CatalogDtos.SearchRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.SpecValue;
import com.tradepost.procure.catalog.web.CatalogDtos.ThumbnailRequest;
import com.tradepost.procure.config.TradepostProperties;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService service;
    private final CatalogImportService imports;
    private final TradepostProperties properties;

    public CatalogController(CatalogService service, CatalogImportService imports, TradepostProperties properties) {
        this.service = service;
        this.imports = imports;
        this.properties = properties;
    }

    @PostMapping("/items")
    public ResponseEntity<Created> create(@Valid @RequestBody CreateItemRequest request) {
        long id = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(id, "DRAFT"));
    }

    @GetMapping("/items")
    public List<Map<String, Object>> search(@RequestParam(defaultValue = "") String q,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false) String dir,
                                            @RequestParam(defaultValue = "25") int limit) {
        return service.search(q, sort, dir, limit);
    }

    @PostMapping("/searches")
    public ResponseEntity<EntityId> saveSearch(@Valid @RequestBody SearchRequest request) {
        long id = service.saveSearch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EntityId(id));
    }

    @GetMapping("/searches/{searchId}/results")
    public List<Map<String, Object>> savedResults(@PathVariable long searchId) {
        return service.savedResults(searchId);
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
    public ResponseEntity<EntityId> registerMedia(@PathVariable long id,
                                                  @Valid @RequestBody MediaRequest request) {
        long mediaId = service.registerMedia(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EntityId(mediaId));
    }

    @GetMapping("/items/{id}/media/{mediaId}")
    public byte[] media(@PathVariable long id, @PathVariable long mediaId, HttpServletResponse response)
            throws IOException {
        Media media = service.media(id, mediaId);
        byte[] content = service.mediaContent(media);
        response.setContentType(media.contentType());
        response.setHeader(properties.downloads().mediaHeader(), "inline; filename=\"" + media.storedName() + "\"");
        return content;
    }

    @PostMapping("/items/{id}/thumbnails")
    public ResponseEntity<EntityId> registerThumbnail(@PathVariable long id,
                                                      @Valid @RequestBody ThumbnailRequest request) {
        long thumbId = service.registerThumbnail(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EntityId(thumbId));
    }

    @GetMapping("/items/{id}/thumbnails/{thumbId}")
    public byte[] thumbnail(@PathVariable long id, @PathVariable long thumbId, HttpServletResponse response)
            throws IOException {
        response.setContentType("image/png");
        return service.thumbnailContent(id, thumbId);
    }

    @PostMapping("/items/{id}/media/fetch")
    public FetchResult fetchMedia(@PathVariable long id, @Valid @RequestBody FetchRequest request) {
        return service.fetchRemoteMedia(id, request.sourceUrl());
    }

    @PutMapping("/items/{id}/prefs")
    public PrefsSaved savePrefs(@PathVariable long id, @Valid @RequestBody PrefsRequest request) {
        return service.savePrefs(id, request);
    }

    @GetMapping("/items/{id}/label")
    public LabelText label(@PathVariable long id) {
        return service.label(id);
    }

    @PostMapping(value = "/import", consumes = {"application/xml", "text/xml", "text/csv", "text/plain"})
    public FeedStaged stageFeed(@RequestParam(defaultValue = "xml") String format, @RequestBody String payload) {
        long id = imports.stage(format, payload);
        return new FeedStaged(id, format);
    }

    @PostMapping("/feeds/{feedId}/process")
    public FeedProcessed processFeed(@PathVariable long feedId) {
        return new FeedProcessed(feedId, imports.process(feedId));
    }

    @GetMapping("/items/{id}/spec/summary")
    public SpecValue specSummary(@PathVariable long id) {
        return service.specSummary(id);
    }
}
