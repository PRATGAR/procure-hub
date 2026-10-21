package com.tradepost.procure.catalog.service;

import com.tradepost.procure.catalog.Item;
import com.tradepost.procure.catalog.repo.CatalogRepository;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class FeedItems {

    private static final Pattern SKU = Pattern.compile("^[A-Z0-9-]{4,24}$");

    private final CatalogRepository repo;

    public FeedItems(CatalogRepository repo) {
        this.repo = repo;
    }

    public boolean accept(String sku, String name, String category, long priceCents, String currency) {
        if (sku == null || !SKU.matcher(sku).matches() || name == null || name.isBlank()) {
            return false;
        }
        Optional<String> code = repo.categoryCode(category);
        if (code.isEmpty() || priceCents < 1 || currency == null || currency.length() != 3) {
            return false;
        }
        if (repo.skuTaken(sku)) {
            return false;
        }
        repo.insertItem(new Item(0, sku, name.trim(), null, code.get(), null, priceCents, currency, null, "DRAFT"));
        return true;
    }
}
