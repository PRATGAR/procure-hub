package com.tradepost.procure.report;

public record SavedFilter(long id, String name, String entity, String whereClause) {
}
