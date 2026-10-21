package com.tradepost.procure.directory.web;

import com.tradepost.procure.directory.OrgUnit;
import com.tradepost.procure.directory.SsoProvider;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class DirectoryDtos {

    private DirectoryDtos() {
    }

    public record SsoLinkRequest(
            @Min(1) long userRef,
            @NotNull SsoProvider provider,
            @NotBlank @Size(max = 80) String externalId,
            @NotNull OrgUnit unit) {
    }

    public record ResolveRequest(@Min(1) long linkId) {
    }

    public record UserQuery(@NotBlank @Pattern(regexp = "^[A-Za-z0-9._*@-]{1,64}$") String uid) {
    }
}
