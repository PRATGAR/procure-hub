package com.tradepost.procure.rfq.web;

import com.tradepost.procure.rfq.Incoterm;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public final class RfqDtos {

    private RfqDtos() {
    }

    public record CreateRfqRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 2000) String description,
            @Size(max = 24) String categoryCode,
            @NotBlank @Size(max = 40) String region,
            @NotNull @Future LocalDate closesAt) {
    }

    public record Landing(long rfqId, String title, String status, String closesAt, String invitation) {
    }

    public record BidAccepted(long bidId) {
    }

    public record QuestionAccepted(long questionId) {
    }

    public record InviteRequest(@NotEmpty @Size(max = 20) List<Long> supplierIds) {
    }

    public record BidRequest(
            @Min(1) long amountCents,
            @NotBlank @Size(max = 12) String currency,
            @NotNull Incoterm incoterm,
            @Size(max = 600) String notes) {
    }

    public record QuestionRequest(@NotBlank @Size(max = 800) String question) {
    }

    public record AwardRequest(@Min(1) long bidId) {
    }
}
