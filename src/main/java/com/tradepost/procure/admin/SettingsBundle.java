package com.tradepost.procure.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SettingsBundle implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HashMap<String, String> entries;

    public SettingsBundle(Map<String, String> entries) {
        this.entries = new HashMap<>(entries);
    }

    public Map<String, String> entries() {
        return entries;
    }
}
