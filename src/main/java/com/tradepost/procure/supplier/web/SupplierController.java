package com.tradepost.procure.supplier.web;

import com.tradepost.procure.supplier.Contact;
import com.tradepost.procure.supplier.Supplier;
import com.tradepost.procure.supplier.service.SupplierService;
import com.tradepost.procure.supplier.web.SupplierDtos.BankAccountRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ContactRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.OnboardRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ProfileRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> onboard(@Valid @RequestBody OnboardRequest request) {
        long id = service.onboard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "PENDING"));
    }

    @GetMapping
    public List<Supplier> search(@RequestParam(defaultValue = "") String q,
                                 @RequestParam(required = false) String country) {
        return service.search(q, country);
    }

    @GetMapping("/lookup")
    public Map<String, Object> lookup(@RequestParam String taxId) {
        return service.lookupByTaxId(taxId);
    }

    @GetMapping("/{id}")
    public Supplier get(@PathVariable long id) {
        return service.get(id);
    }

    @PutMapping("/{id}/profile")
    public Map<String, Object> updateProfile(@PathVariable long id, @Valid @RequestBody ProfileRequest request) {
        service.updateProfile(id, request);
        return Map.of("id", id, "updated", true);
    }

    @PostMapping("/{id}/contacts")
    public ResponseEntity<Map<String, Object>> addContact(@PathVariable long id,
                                                          @Valid @RequestBody ContactRequest request) {
        long contactId = service.addContact(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", contactId));
    }

    @GetMapping("/{id}/contacts")
    public List<Contact> contacts(@PathVariable long id, @RequestParam(defaultValue = "") String q) {
        return service.contacts(id, q);
    }

    @PutMapping("/{id}/bank-account")
    public Map<String, Object> updateBankAccount(@PathVariable long id,
                                                 @Valid @RequestBody BankAccountRequest request) {
        service.updateBankAccount(id, request);
        return Map.of("supplierId", id, "status", "PENDING");
    }

    @PostMapping("/{id}/website-check")
    public Map<String, Object> checkWebsite(@PathVariable long id) {
        return service.checkWebsite(id);
    }
}
