package com.tradepost.procure.document.web;

import com.tradepost.procure.document.OwnerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class DocumentDtos {

    private DocumentDtos() {
    }

    public record RegisterRequest(
            @NotNull OwnerType ownerType,
            @Min(1) long ownerId,
            @NotBlank @Size(max = 255) String fileName,
            @Size(max = 200) String displayName,
            @Size(max = 200) String convertOpts) {
    }
}
