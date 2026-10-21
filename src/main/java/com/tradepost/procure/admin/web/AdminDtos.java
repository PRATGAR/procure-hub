package com.tradepost.procure.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record BundleRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Pattern(regexp = "^[0-9a-f]{2,7000}$") String blobHex) {
    }

    public record SettingsRequest(
            @NotNull @Size(max = 600) String value,
            @Size(max = 80) String changedBy) {
    }

    public record TemplateTestRequest(@NotBlank @Size(max = 500) String template) {
    }
}
