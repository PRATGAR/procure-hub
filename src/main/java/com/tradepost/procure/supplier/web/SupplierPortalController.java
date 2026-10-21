package com.tradepost.procure.supplier.web;

import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.supplier.Supplier;
import com.tradepost.procure.supplier.service.SupplierPortalPages;
import com.tradepost.procure.supplier.service.SupplierService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/portal/suppliers")
public class SupplierPortalController {

    private final SupplierService service;
    private final SupplierPortalPages pages;
    private final PageWriter writer;

    public SupplierPortalController(SupplierService service, SupplierPortalPages pages, PageWriter writer) {
        this.service = service;
        this.pages = pages;
        this.writer = writer;
    }

    @GetMapping("/{id}")
    public void profile(@PathVariable long id, HttpServletResponse response) throws IOException {
        Supplier supplier = service.get(id);
        writer.html(response, pages.profile(supplier));
    }

    @GetMapping("/{id}/contacts")
    public void contacts(@PathVariable long id, HttpServletResponse response) throws IOException {
        Supplier supplier = service.get(id);
        writer.html(response, pages.contacts(supplier, service.contacts(id, "")));
    }
}
