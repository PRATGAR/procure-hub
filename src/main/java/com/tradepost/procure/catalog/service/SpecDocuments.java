package com.tradepost.procure.catalog.service;

import com.tradepost.procure.catalog.Item;
import com.tradepost.procure.common.Html;

final class SpecDocuments {

    private SpecDocuments() {
    }

    static String build(Item item) {
        return "<spec><attributes>"
                + attribute("sku", item.sku())
                + attribute("name", item.name())
                + attribute("category", item.categoryCode())
                + attribute("currency", item.currency())
                + attribute("status", item.status())
                + attribute("tags", item.tags())
                + "</attributes><internal><costCenter>CC-4711</costCenter><marginBp>1800</marginBp></internal></spec>";
    }

    private static String attribute(String name, String value) {
        return "<attribute name=\"" + name + "\" value=\"" + Html.escape(value) + "\"/>";
    }
}
