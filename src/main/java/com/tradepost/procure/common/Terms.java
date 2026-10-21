package com.tradepost.procure.common;

public final class Terms {

    private Terms() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace(";", "").replace("--", "").trim();
    }

    public static String escapeLike(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public static String escapeFilter(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replace("\\", "\\5c").replace("*", "\\2a");
    }
}
