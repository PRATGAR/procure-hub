package com.tradepost.procure.catalog.service;

import com.tradepost.procure.catalog.Item;
import com.tradepost.procure.catalog.ItemRef;
import com.tradepost.procure.catalog.Media;
import com.tradepost.procure.catalog.Thumbnail;
import com.tradepost.procure.catalog.repo.CatalogRepository;
import com.tradepost.procure.catalog.web.CatalogDtos.CreateItemRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.ItemDetail;
import com.tradepost.procure.catalog.web.CatalogDtos.MediaRequest;
import com.tradepost.procure.catalog.web.CatalogDtos.ThumbnailRequest;
import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.FileNames;
import com.tradepost.procure.common.FileStore;
import com.tradepost.procure.common.Html;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.common.QueryFragments;
import com.tradepost.procure.common.SearchSupport;
import com.tradepost.procure.common.Terms;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

@Service
public class CatalogService {

    private static final String SEARCH_SELECT =
            "SELECT id, sku, name, category_code, unit_price_cents, currency FROM catalog_items";

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "name", "name",
            "sku", "sku",
            "price", "unit_price_cents");

    private final CatalogRepository repo;
    private final SearchSupport search;
    private final FileStore store;
    private final OutboundHttp outbound;
    private final AuditWriter audit;

    public CatalogService(CatalogRepository repo, SearchSupport search, FileStore store,
                          OutboundHttp outbound, AuditWriter audit) {
        this.repo = repo;
        this.search = search;
        this.store = store;
        this.outbound = outbound;
        this.audit = audit;
    }

    public long create(CreateItemRequest request) {
        if (repo.skuExists(request.sku())) {
            throw new ConflictException("an item with this sku already exists");
        }
        String category = repo.categoryCode(request.categoryCode())
                .orElseThrow(() -> new InvalidRequestException("unknown category"));
        Item item = new Item(0, request.sku(), request.name().trim(), request.description(), category,
                request.tags(), request.unitPriceCents(), request.currency(), request.supplierId(), "DRAFT");
        long id = repo.insertItem(item);
        audit.append(Actors.current(), "catalog.item.created", "item " + id);
        return id;
    }

    public List<Map<String, Object>> search(String q, String sort, String dir, int limit) {
        String where = " WHERE status = 'ACTIVE' AND name LIKE '%" + Terms.escapeLike(q) + "%'";
        String column = SORT_COLUMNS.getOrDefault(sort == null ? "name" : sort, "name");
        String order = QueryFragments.orderBy(column, dir);
        return search.page(SEARCH_SELECT, where, order, limit);
    }

    public ItemDetail detail(long id) {
        Item item = repo.findItem(id);
        List<ItemRef> related = repo.relatedByTag(id, firstTag(item.tags()));
        List<ItemRef> siblings = repo.siblings(id, item.categoryCode());
        return new ItemDetail(item, related, siblings, repo.mediaFor(id));
    }

    public List<Item> inCategory(long categoryId, String dir) {
        return repo.byCategory(categoryId, dir);
    }

    public long registerMedia(long itemId, MediaRequest request) {
        repo.findItem(itemId);
        String storedName = FileNames.sanitizeFileName(request.fileName());
        if (storedName.isBlank()) {
            throw new InvalidRequestException("file name is required");
        }
        String contentType = request.contentType() == null ? "application/octet-stream" : request.contentType();
        return repo.insertMedia(itemId, storedName, request.caption(), contentType);
    }

    public Media media(long itemId, long mediaId) {
        return repo.findMedia(itemId, mediaId);
    }

    public byte[] mediaContent(Media media) throws IOException {
        return store.read(FileStore.MEDIA, media.storedName());
    }

    public long registerThumbnail(long itemId, ThumbnailRequest request) {
        repo.findItem(itemId);
        return repo.insertThumbnail(itemId, request.fileName());
    }

    public byte[] thumbnailContent(long itemId, long thumbId) throws IOException {
        Thumbnail thumbnail = repo.findThumbnail(itemId, thumbId);
        return store.read(FileStore.THUMBS, thumbnail.fileName());
    }

    public Map<String, Object> fetchRemoteMedia(long itemId, String sourceUrl) {
        repo.findItem(itemId);
        String body = outbound.fetchText(sourceUrl);
        return Map.of("itemId", itemId, "bytes", body == null ? 0 : body.length());
    }

    public String card(long id) {
        Item item = repo.findItem(id);
        return "<article class=\"item-card\"><h2>" + Html.escape(item.name()) + "</h2>"
                + "<p>" + Html.escape(item.description()) + "</p>"
                + "<footer>" + Html.escape(item.sku()) + " &middot; "
                + item.unitPriceCents() / 100 + " " + Html.escape(item.currency()) + "</footer></article>";
    }

    public String specSummary(long id, String field) {
        Item item = repo.findItem(id);
        String xml = SpecDocuments.build(item);
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/spec/attributes/attribute[@name='" + field + "']/@value";
        try {
            return xpath.evaluateExpression(expression, new InputSource(new StringReader(xml)), String.class);
        } catch (XPathExpressionException e) {
            throw new InvalidRequestException("unknown specification field");
        }
    }

    private String firstTag(String tags) {
        if (tags == null || tags.isBlank()) {
            return "";
        }
        return tags.split(",")[0].trim();
    }
}
