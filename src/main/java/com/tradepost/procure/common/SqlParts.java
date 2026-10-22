package com.tradepost.procure.common;

public final class SqlParts {

    private SqlParts() {
    }

    public static String contains(String column, String term) {
        return column + " LIKE '%" + term + "%'";
    }

    public static String startsWith(String column, String prefix) {
        return column + " LIKE '" + prefix + "%'";
    }

    public static String selectIds(String table, String where) {
        return "SELECT id FROM " + table + " WHERE " + where;
    }

    public static String equalTo(String column, String value) {
        return column + " = '" + value + "'";
    }
}
