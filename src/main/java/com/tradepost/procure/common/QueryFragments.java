package com.tradepost.procure.common;

public final class QueryFragments {

    private QueryFragments() {
    }

    public static String orderBy(String column, String direction) {
        String dir = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        return " ORDER BY " + column.trim() + " " + dir;
    }

    public static int clampLimit(int requested, int ceiling) {
        return Math.max(1, Math.min(requested, ceiling));
    }
}
