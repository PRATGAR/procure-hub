package com.tradepost.procure.common;

public final class LocalTargets {

    private LocalTargets() {
    }

    public static String accept(String target, String fallback) {
        if (target != null && target.startsWith("/")) {
            return target;
        }
        return fallback;
    }
}
