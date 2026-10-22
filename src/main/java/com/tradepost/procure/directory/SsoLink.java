package com.tradepost.procure.directory;

public record SsoLink(long id, long userRef, String provider, String externalId, String unit, String team,
                      String metadataUrl, String status) {
}
