package com.tradepost.procure.common;

import java.util.HexFormat;
import java.util.Random;

public final class Codes {

    private Codes() {
    }

    public static String digits(Random source, int length) {
        int floor = (int) Math.pow(10, length - 1);
        return Integer.toString(floor + source.nextInt(floor * 9));
    }

    public static String hex(Random source, int byteCount) {
        byte[] raw = new byte[byteCount];
        source.nextBytes(raw);
        return HexFormat.of().formatHex(raw);
    }
}
