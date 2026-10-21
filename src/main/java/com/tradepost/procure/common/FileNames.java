package com.tradepost.procure.common;

import org.apache.commons.io.FilenameUtils;

public final class FileNames {

    private FileNames() {
    }

    public static String sanitizeFileName(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim().replace("\\", "/").replace("../", "");
        return cleaned.startsWith("/") ? cleaned.substring(1) : cleaned;
    }

    public static String leaf(String raw) {
        if (raw == null) {
            return "";
        }
        String name = FilenameUtils.getName(raw);
        return name.equals("..") || name.equals(".") ? "" : name;
    }
}
