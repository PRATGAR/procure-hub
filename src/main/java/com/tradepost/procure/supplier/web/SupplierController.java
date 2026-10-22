package com.tradepost.procure.supplier.web;

import com.tradepost.procure.common.NumericId;
import com.tradepost.procure.supplier.Contact;
import com.tradepost.procure.supplier.Supplier;
import com.tradepost.procure.supplier.service.SupplierService;
import com.tradepost.procure.supplier.web.SupplierDtos.BankAccountRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.BankStatus;
import com.tradepost.procure.supplier.web.SupplierDtos.CertificateRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ContactRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.Created;
import com.tradepost.procure.supplier.web.SupplierDtos.EntityId;
import com.tradepost.procure.supplier.web.SupplierDtos.OnboardRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ProfileRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ProfileUpdated;
import com.tradepost.procure.supplier.web.SupplierDtos.RegistryCheck;
import com.tradepost.procure.supplier.web.SupplierDtos.WebsiteCheck;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
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
    public ResponseEntity<Created> onboard(@Valid @RequestBody OnboardRequest request) {
        long id = service.onboard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(id, "PENDING"));
    }

    @GetMapping
    public List<Supplier> search(@RequestParam(defaultValue = "") SearchTerm q,
                                 @RequestParam(required = false) String country) {
        return service.search(q.value(), country);
    }

    @GetMapping("/{id}")
    public Supplier get(@PathVariable NumericId id) {
        return service.get(id);
    }

    @GetMapping("/{id}/similar")
    public List<Supplier> similar(@PathVariable long id) {
        return service.similar(id);
    }

    @GetMapping("/{id}/registry-check")
    public RegistryCheck registryCheck(@PathVariable long id) {
        return service.registryCheck(id);
    }

    @PutMapping("/{id}/profile")
    public ProfileUpdated updateProfile(@PathVariable long id, @Valid @RequestBody ProfileRequest request) {
        service.updateProfile(id, request);
        return new ProfileUpdated(id, true);
    }

    @PostMapping("/{id}/contacts")
    public ResponseEntity<EntityId> addContact(@PathVariable long id,
                                               @Valid @RequestBody ContactRequest request) {
        long contactId = service.addContact(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EntityId(contactId));
    }

    @GetMapping("/{id}/contacts")
    public List<Contact> contacts(@PathVariable long id) {
        return service.contacts(id);
    }

    @GetMapping("/contacts/{contactId}/aliases")
    public List<Contact> aliases(@PathVariable long contactId) {
        return service.aliases(contactId);
    }

    @PutMapping("/{id}/bank-account")
    public BankStatus updateBankAccount(@PathVariable long id,
                                        @Valid @RequestBody BankAccountRequest request) {
        service.updateBankAccount(id, request);
        return new BankStatus(id, "PENDING");
    }

    @PostMapping("/{id}/website-check")
    public WebsiteCheck checkWebsite(@PathVariable long id) {
        return service.checkWebsite(id);
    }

    @PostMapping("/{id}/certificates")
    public ResponseEntity<EntityId> addCertificate(@PathVariable long id,
                                                   @Valid @RequestBody CertificateRequest request) {
        long certificateId = service.addCertificate(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EntityId(certificateId));
    }

    @GetMapping("/{id}/certificates/{certificateId}/file")
    public byte[] certificateFile(@PathVariable long id, @PathVariable long certificateId,
                                  HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        return service.certificateFile(id, certificateId);
    }
}
