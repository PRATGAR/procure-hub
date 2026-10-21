package com.tradepost.procure.integration.service;

import java.io.ObjectInputFilter;

final class SerialFilters {

    private static final String PARTNER_EVENTS =
            "com.tradepost.procure.integration.PartnerEvent;java.lang.String;java.lang.Long;java.lang.Integer;!*";

    private SerialFilters() {
    }

    static ObjectInputFilter partnerEvents() {
        return ObjectInputFilter.Config.createFilter(PARTNER_EVENTS);
    }
}
