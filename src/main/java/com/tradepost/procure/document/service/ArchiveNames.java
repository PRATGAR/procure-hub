package com.tradepost.procure.document.service;

final class ArchiveNames {

    private ArchiveNames() {
    }

    static String generate() {
        return "ARC-" + Long.toHexString(System.nanoTime()) + ".zip";
    }
}
