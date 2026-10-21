package com.tradepost.procure.rfq;

import java.time.LocalDate;

public record Rfq(
        long id,
        String title,
        String description,
        String categoryCode,
        String region,
        String status,
        LocalDate closesAt,
        Long awardedBidId,
        String createdBy) {
}
