package com.tradepost.procure.admin;

import com.tradepost.procure.common.FileStore;

public enum PurgeTarget {
    EXPORTS(FileStore.EXPORTS),
    ARCHIVES(FileStore.ARCHIVES),
    ATTACHMENTS(FileStore.ATTACHMENTS);

    private final String area;

    PurgeTarget(String area) {
        this.area = area;
    }

    public String area() {
        return area;
    }
}
