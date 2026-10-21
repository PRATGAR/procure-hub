package com.tradepost.procure.supplier.service;

import com.tradepost.procure.common.Html;
import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.supplier.Contact;
import com.tradepost.procure.supplier.Supplier;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SupplierPortalPages {

    private final PageWriter writer;

    public SupplierPortalPages(PageWriter writer) {
        this.writer = writer;
    }

    public String profile(Supplier supplier) {
        String heading = Html.escape(supplier.displayName() == null ? supplier.legalName() : supplier.displayName());
        String body = "<h1>" + heading + "</h1>"
                + "<p class=\"country\">" + Html.escape(supplier.country()) + "</p>"
                + "<section class=\"about\">" + supplier.description() + "</section>"
                + "<p><a href=\"/portal/suppliers/" + supplier.id() + "/contacts\">Contacts</a></p>";
        return writer.page(heading, body);
    }

    public String contacts(Supplier supplier, List<Contact> contacts) {
        StringBuilder rows = new StringBuilder();
        for (Contact contact : contacts) {
            rows.append("<li><strong>").append(contact.name()).append("</strong> (")
                    .append(contact.role()).append(")<br>")
                    .append(contact.note() == null ? "" : contact.note())
                    .append("</li>");
        }
        String heading = Html.escape(supplier.legalName());
        String body = "<h1>Contacts at " + heading + "</h1><ul>" + rows + "</ul>";
        return writer.page(heading, body);
    }
}
