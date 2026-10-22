package com.tradepost.procure.supplier.web;

import com.tradepost.procure.supplier.ContactRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class SupplierDtos {

    private SupplierDtos() {
    }

    public record OnboardRequest(
            @NotBlank @Size(max = 160) String legalName,
            @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String country,
            @NotBlank @Pattern(regexp = "^[A-Z0-9]{8,14}$") String taxId,
            @Size(max = 300) String website,
            @Size(max = 2000) String description) {
    }

    public record ProfileRequest(
            @Size(max = 160) String displayName,
            @Size(max = 2000) String description,
            @Size(max = 300) String website,
            @Size(max = 20) String taxId) {
    }

    public record CertificateRequest(
            @NotBlank @Size(max = 120) String issuer,
            @NotBlank @Size(max = 200) String fileName) {
    }

    public record Created(long id, String status) {
    }

    public record EntityId(long id) {
    }

    public record ProfileUpdated(long id, boolean updated) {
    }

    public record BankStatus(long supplierId, String status) {
    }

    public record WebsiteCheck(long supplierId, boolean reachable, int bytes) {
    }

    public record RegistryCheck(long supplierId, int registrations) {
    }

    public record ContactRequest(
            @NotBlank @Size(max = 120) String name,
            @Email @Size(max = 160) String email,
            @NotNull ContactRole role,
            @Size(max = 400) String note) {
    }

    public record BankAccountRequest(
            @NotBlank @Size(max = 160) String holder,
            @NotBlank @Pattern(regexp = "^[A-Z0-9]{8,11}$") String bic,
            @Pattern(regexp = "^[0-9]{4,8}$") String ibanTail) {
    }
}
