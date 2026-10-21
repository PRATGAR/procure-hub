package com.tradepost.procure.common;

import org.apache.commons.io.FilenameUtils;

public final class StoredNames {

    private StoredNames() {
    }

    public static String from(String original) {
        String base = FilenameUtils.getName(original == null ? "" : original);
        if (base.isBlank() || base.equals(".") || base.equals("..")) {
            base = "file.bin";
        }
        return Long.toHexString(System.nanoTime()) + "-" + base;
    }
}
