package com.tradepost.procure.common;

import java.nio.file.Path;

public final class SafeJoin {

    private SafeJoin() {
    }

    public static Path within(Path base, String name) {
        Path candidate = base.resolve(name).normalize();
        if (!candidate.toString().startsWith(base.toString())) {
            throw new IllegalArgumentException("path leaves the base directory");
        }
        return candidate;
    }
}
