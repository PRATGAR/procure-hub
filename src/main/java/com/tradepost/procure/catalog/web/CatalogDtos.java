package com.tradepost.procure.catalog.web;

import com.tradepost.procure.catalog.ItemRef;
import com.tradepost.procure.catalog.Item;
import com.tradepost.procure.catalog.Media;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CreateItemRequest(
            @NotBlank @Pattern(regexp = "^[A-Z0-9-]{4,24}$") String sku,
            @NotBlank @Size(max = 160) String name,
            @Size(max = 2000) String description,
            @NotBlank @Size(max = 24) String categoryCode,
            @Size(max = 300) String tags,
            @Min(1) long unitPriceCents,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
            Long supplierId) {
    }

    public record MediaRequest(
            @NotBlank @Size(max = 200) String fileName,
            @Size(max = 160) String caption,
            @Pattern(regexp = "^[a-z]+/[a-z0-9.+-]+$") String contentType) {
    }

    public record ThumbnailRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{1,60}\\.(png|jpg)$") String fileName) {
    }

    public record FetchRequest(@NotBlank @Size(max = 400) String sourceUrl) {
    }

    public record ItemDetail(Item item, List<ItemRef> related, List<ItemRef> siblings, List<Media> media) {
    }
}
