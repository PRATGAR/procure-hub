package com.tradepost.procure.supplier.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.Html;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.supplier.Contact;
import com.tradepost.procure.supplier.Supplier;
import com.tradepost.procure.supplier.repo.ContactRepository;
import com.tradepost.procure.supplier.repo.SupplierRepository;
import com.tradepost.procure.supplier.web.SupplierDtos.BankAccountRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ContactRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.OnboardRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ProfileRequest;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository suppliers;
    private final ContactRepository contacts;
    private final OutboundHttp outbound;
    private final AuditWriter audit;
    private final ApplicationEventPublisher events;

    public SupplierService(SupplierRepository suppliers, ContactRepository contacts, OutboundHttp outbound,
                           AuditWriter audit, ApplicationEventPublisher events) {
        this.suppliers = suppliers;
        this.contacts = contacts;
        this.outbound = outbound;
        this.audit = audit;
        this.events = events;
    }

    public long onboard(OnboardRequest request) {
        if (suppliers.existsByTaxId(request.taxId())) {
            throw new ConflictException("a supplier with this tax id is already registered");
        }
        long id = suppliers.insert(request.legalName().trim(), request.country(), request.taxId(),
                request.website(), request.description());
        log.info("supplier onboarded taxId=" + request.taxId() + " country=" + request.country());
        audit.append(Actors.current(), "supplier.onboard", "supplier " + id);
        return id;
    }

    public List<Supplier> search(String q, String country) {
        List<Supplier> found = suppliers.search(q);
        if (country == null || country.isBlank()) {
            return found;
        }
        return found.stream().filter(s -> s.country().equalsIgnoreCase(country)).toList();
    }

    public Map<String, Object> lookupByTaxId(String taxId) {
        boolean registered = suppliers.existsByTaxId(taxId);
        return Map.of("taxId", taxId, "registered", registered);
    }

    public Supplier get(long id) {
        return suppliers.findById(id);
    }

    public void updateProfile(long id, ProfileRequest request) {
        Supplier supplier = get(id);
        String displayName = request.displayName() == null || request.displayName().isBlank()
                ? supplier.legalName() : request.displayName().trim();
        suppliers.updateProfile(id, displayName, request.description(), request.website());
        audit.append(Actors.current(), "supplier.profile", "supplier " + id);
    }

    public long addContact(long supplierId, ContactRequest request) {
        get(supplierId);
        String name = Html.escape(request.name().trim());
        String note = Html.escape(request.note());
        return contacts.insert(supplierId, name, request.email(), request.role().name(), note);
    }

    public List<Contact> contacts(long supplierId, String q) {
        get(supplierId);
        if (q == null || q.isBlank()) {
            return contacts.forSupplier(supplierId);
        }
        return contacts.search(supplierId, q);
    }

    public void updateBankAccount(long supplierId, BankAccountRequest request) {
        Supplier supplier = get(supplierId);
        if ("BLOCKED".equals(supplier.status())) {
            throw new ConflictException("blocked suppliers cannot change bank details");
        }
        suppliers.saveBankAccount(supplierId, request.holder().trim(), request.bic(), request.ibanTail());
        events.publishEvent(new BankDetailsChanged(supplierId, request.holder().trim(), request.bic()));
    }

    public Map<String, Object> checkWebsite(long id) {
        Supplier supplier = get(id);
        if (supplier.website() == null || supplier.website().isBlank()) {
            throw new InvalidRequestException("supplier has no website on file");
        }
        String body = outbound.fetchText(supplier.website());
        return Map.of("supplierId", id, "reachable", body != null, "bytes", body == null ? 0 : body.length());
    }
}
