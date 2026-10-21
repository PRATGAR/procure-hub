package com.tradepost.procure.common;

public enum SortDirection {
    ASC,
    DESC;

    public static SortDirection of(String raw) {
        return "desc".equalsIgnoreCase(raw) ? DESC : ASC;
    }
}
