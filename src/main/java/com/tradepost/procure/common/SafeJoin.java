package com.tradepost.procure.common;

import java.nio.file.Path;

public final class SafeJoin {

    private SafeJoin() {
    }

    public static Path within(Path base, String name) {
        if (Path.of(name).isAbsolute()) {
            throw new IllegalArgumentException("absolute names are not accepted");
        }
        Path candidate = base.resolve(name).normalize();
        return candidate;
    }
}
