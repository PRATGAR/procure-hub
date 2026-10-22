package com.tradepost.procure.catalog.service;

import com.tradepost.procure.common.XmlFactories;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class XmlFeedParser implements FeedParser {

    private final FeedItems items;

    public XmlFeedParser(FeedItems items) {
        this.items = items;
    }

    @Override
    public int parse(String payload) throws Exception {
        DocumentBuilderFactory factory = XmlFactories.feedFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(payload)));
        NodeList nodes = document.getElementsByTagName("item");
        int imported = 0;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            long price = Long.parseLong(element.getAttribute("priceCents"));
            if (items.accept(element.getAttribute("sku"), element.getAttribute("name"),
                    element.getAttribute("category"), price, element.getAttribute("currency"))) {
                imported++;
            }
        }
        return imported;
    }
}
