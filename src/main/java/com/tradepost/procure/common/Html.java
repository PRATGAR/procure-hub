package com.tradepost.procure.common;

import org.springframework.web.util.HtmlUtils;

public final class Html {

    private Html() {
    }

    public static String escape(String raw) {
        return raw == null ? "" : HtmlUtils.htmlEscape(raw);
    }
}
