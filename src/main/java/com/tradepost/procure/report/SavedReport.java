package com.tradepost.procure.report;

public record SavedReport(long id, String name, String entity, String whereClause, String description, String owner) {
}
