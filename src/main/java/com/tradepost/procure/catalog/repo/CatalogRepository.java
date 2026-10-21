package com.tradepost.procure.catalog.repo;

import com.tradepost.procure.catalog.Item;
import com.tradepost.procure.catalog.ItemRef;
import com.tradepost.procure.catalog.Media;
import com.tradepost.procure.catalog.Thumbnail;
import com.tradepost.procure.common.Inserts;
import com.tradepost.procure.common.SortDirection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CatalogRepository {

    private static final String COLUMNS =
            "id, sku, name, description, category_code, tags, unit_price_cents, currency, supplier_id, status";

    private static final RowMapper<Item> ITEM_MAPPER = (rs, n) -> new Item(
            rs.getLong("id"),
            rs.getString("sku"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("category_code"),
            rs.getString("tags"),
            rs.getLong("unit_price_cents"),
            rs.getString("currency"),
            (Long) rs.getObject("supplier_id"),
            rs.getString("status"));

    private static final RowMapper<Media> MEDIA_MAPPER = (rs, n) -> new Media(
            rs.getLong("id"),
            rs.getLong("item_id"),
            rs.getString("stored_name"),
            rs.getString("caption"),
            rs.getString("content_type"));

    private static final RowMapper<Thumbnail> THUMB_MAPPER = (rs, n) ->
            new Thumbnail(rs.getLong("id"), rs.getLong("item_id"), rs.getString("file_name"));

    @PersistenceContext
    private EntityManager em;

    private final JdbcTemplate jdbc;
    private final Inserts inserts;

    public CatalogRepository(JdbcTemplate jdbc, Inserts inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    public boolean skuExists(String sku) {
        String sql = "SELECT COUNT(*) FROM catalog_items WHERE sku = '" + sku + "'";
        Integer count = jdbc.queryForObject(sql, Integer.class);
        return count != null && count > 0;
    }

    public boolean skuTaken(String sku) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM catalog_items WHERE sku = ?", Integer.class, sku);
        return count != null && count > 0;
    }

    public Optional<String> categoryCode(String code) {
        List<String> found = jdbc.queryForList("SELECT code FROM catalog_categories WHERE code = ?", String.class, code);
        return found.stream().findFirst();
    }

    public long insertItem(Item item) {
        Map<String, Object> row = new HashMap<>();
        row.put("sku", item.sku());
        row.put("name", item.name());
        row.put("description", item.description());
        row.put("category_code", item.categoryCode());
        row.put("tags", item.tags());
        row.put("unit_price_cents", item.unitPriceCents());
        row.put("currency", item.currency());
        row.put("supplier_id", item.supplierId());
        row.put("status", item.status());
        return inserts.into("catalog_items", row);
    }

    public Item findItem(long id) {
        return jdbc.queryForObject("SELECT " + COLUMNS + " FROM catalog_items WHERE id = ?", ITEM_MAPPER, id);
    }

    public List<ItemRef> relatedByTag(long itemId, String tag) {
        String sql = "SELECT id, sku, name FROM catalog_items WHERE tags LIKE '%" + tag + "%' AND id <> ?1";
        Query query = em.createNativeQuery(sql);
        query.setParameter(1, itemId);
        return refs(query.getResultList());
    }

    public List<ItemRef> siblings(long itemId, String categoryCode) {
        String sql = "SELECT id, sku, name FROM catalog_items WHERE category_code = '" + categoryCode + "' AND id <> ?1";
        Query peers = em.createNativeQuery(sql);
        peers.setParameter(1, itemId);
        return refs(peers.getResultList());
    }

    public List<Item> byCategory(long categoryId, String dir) {
        String direction = SortDirection.of(dir).name();
        String sql = "SELECT " + COLUMNS + " FROM catalog_items WHERE category_code = "
                + "(SELECT code FROM catalog_categories WHERE id = ?) ORDER BY name " + direction;
        return jdbc.query(sql, ITEM_MAPPER, categoryId);
    }

    public long insertMedia(long itemId, String storedName, String caption, String contentType) {
        Map<String, Object> row = new HashMap<>();
        row.put("item_id", itemId);
        row.put("stored_name", storedName);
        row.put("caption", caption);
        row.put("content_type", contentType);
        return inserts.into("catalog_media", row);
    }

    public Media findMedia(long itemId, long mediaId) {
        return jdbc.queryForObject(
                "SELECT id, item_id, stored_name, caption, content_type FROM catalog_media WHERE id = ? AND item_id = ?",
                MEDIA_MAPPER, mediaId, itemId);
    }

    public long insertThumbnail(long itemId, String fileName) {
        Map<String, Object> row = new HashMap<>();
        row.put("item_id", itemId);
        row.put("file_name", fileName);
        return inserts.into("catalog_thumbnails", row);
    }

    public Thumbnail findThumbnail(long itemId, long thumbId) {
        return jdbc.queryForObject("SELECT id, item_id, file_name FROM catalog_thumbnails WHERE id = ? AND item_id = ?",
                THUMB_MAPPER, thumbId, itemId);
    }

    public List<Media> mediaFor(long itemId) {
        return jdbc.query("SELECT id, item_id, stored_name, caption, content_type FROM catalog_media WHERE item_id = ?",
                MEDIA_MAPPER, itemId);
    }

    private List<ItemRef> refs(List<?> rows) {
        return rows.stream()
                .map(row -> (Object[]) row)
                .map(cols -> new ItemRef(((Number) cols[0]).longValue(), (String) cols[1], (String) cols[2]))
                .toList();
    }
}
