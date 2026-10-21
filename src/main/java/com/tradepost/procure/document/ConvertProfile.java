package com.tradepost.procure.document;

import java.util.ArrayList;
import java.util.List;

public enum ConvertProfile {
    TEXT("--mode", "text"),
    LAYOUT("--mode", "layout"),
    RAW("--mode", "raw");

    private final List<String> flags;

    ConvertProfile(String... flags) {
        this.flags = List.of(flags);
    }

    public List<String> argv(long documentId) {
        List<String> argv = new ArrayList<>(List.of("text-render", "--doc", String.valueOf(documentId)));
        argv.addAll(flags);
        return argv;
    }
}
