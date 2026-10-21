package com.tradepost.procure.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public final class HostAllowlist {

    private HostAllowlist() {
    }

    public static boolean allows(String url, List<String> hosts) {
        if (url == null) {
            return false;
        }
        try {
            URI uri = new URI(url);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getUserInfo() == null
                    && uri.getHost() != null
                    && hosts.stream().anyMatch(host -> host.equalsIgnoreCase(uri.getHost()));
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
