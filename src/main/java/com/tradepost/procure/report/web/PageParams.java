package com.tradepost.procure.report.web;

import jakarta.validation.constraints.Min;

public record PageParams(@Min(1) Integer limit) {

    public PageParams {
        if (limit == null) {
            limit = 20;
        }
    }
}
