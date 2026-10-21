package com.tradepost.procure.document;

public record Document(
        long id,
        String ownerType,
        long ownerId,
        String fileName,
        String displayName,
        String convertOpts,
        String archiveName,
        String fingerprint,
        String status) {
}
