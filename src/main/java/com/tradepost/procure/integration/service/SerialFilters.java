package com.tradepost.procure.integration.service;

import java.io.ObjectInputFilter;

final class SerialFilters {

    private static final String PARTNER_EVENTS =
            "com.tradepost.procure.integration.PartnerEvent;java.lang.String;java.lang.Long;java.lang.Integer;!*";

    private static final String IMPORT_FORMATS =
            "java.util.*;java.lang.*;java.math.*;java.time.*;java.net.URL;maxdepth=12;maxarray=10000;!*";

    private SerialFilters() {
    }

    static ObjectInputFilter importFormats() {
        return ObjectInputFilter.Config.createFilter(IMPORT_FORMATS);
    }

    static ObjectInputFilter partnerEvents() {
        return ObjectInputFilter.Config.createFilter(PARTNER_EVENTS);
    }
}
