package com.tradepost.procure.catalog.service;

import com.tradepost.procure.common.InvalidRequestException;
import java.io.StringReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

final class SpecQueries {

    private SpecQueries() {
    }

    static String lookup(String xml, String field) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/spec/attributes/attribute[@name='" + field + "']/@value";
        try {
            return xpath.evaluateExpression(expression, new InputSource(new StringReader(xml)), String.class);
        } catch (XPathExpressionException e) {
            throw new InvalidRequestException("unknown specification field");
        }
    }
}
