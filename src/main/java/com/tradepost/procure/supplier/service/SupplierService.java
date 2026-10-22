package com.tradepost.procure.supplier.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.NumericId;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.Html;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.supplier.Certificate;
import com.tradepost.procure.supplier.Contact;
import com.tradepost.procure.supplier.Supplier;
import com.tradepost.procure.supplier.repo.CertificateRepository;
import com.tradepost.procure.supplier.repo.ContactRepository;
import com.tradepost.procure.supplier.repo.SupplierRepository;
import com.tradepost.procure.supplier.web.SupplierDtos.BankAccountRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.CertificateRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ContactRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.OnboardRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.ProfileRequest;
import com.tradepost.procure.supplier.web.SupplierDtos.RegistryCheck;
import com.tradepost.procure.supplier.web.SupplierDtos.WebsiteCheck;
import java.io.IOException;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private final SupplierRepository suppliers;
    private final ContactRepository contacts;
    private final CertificateRepository certificates;
    private final CertificateFiles certificateFiles;
    private final WebsiteProbe probe;
    private final AuditWriter audit;
    private final ApplicationEventPublisher events;

    public SupplierService(SupplierRepository suppliers, ContactRepository contacts,
                           CertificateRepository certificates, CertificateFiles certificateFiles,
                           WebsiteProbe probe, AuditWriter audit, ApplicationEventPublisher events) {
        this.suppliers = suppliers;
        this.contacts = contacts;
        this.certificates = certificates;
        this.certificateFiles = certificateFiles;
        this.probe = probe;
        this.audit = audit;
        this.events = events;
    }

    public long onboard(OnboardRequest request) {
        if (suppliers.countByTaxId(request.taxId()) > 0) {
            throw new ConflictException("a supplier with this tax id is already registered");
        }
        long id = suppliers.insert(request.legalName().trim(), request.country(), request.taxId(),
                request.website(), request.description());
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

    public List<Supplier> similar(long id) {
        Supplier supplier = get(id);
        String stem = supplier.legalName().trim().split("[ \t]+")[0];
        return suppliers.byNameLike(stem);
    }

    public RegistryCheck registryCheck(long id) {
        Supplier supplier = get(id);
        return new RegistryCheck(id, suppliers.countByTaxId(supplier.taxId()));
    }

    public Supplier get(long id) {
        return get(new NumericId(Long.toString(id)));
    }

    public Supplier get(NumericId id) {
        return suppliers.findById(id.value());
    }

    public void updateProfile(long id, ProfileRequest request) {
        Supplier supplier = get(id);
        String displayName = request.displayName() == null || request.displayName().isBlank()
                ? supplier.legalName() : request.displayName().trim();
        suppliers.updateProfile(id, displayName, request.description(), request.website(), request.taxId());
        audit.append(Actors.current(), "supplier.profile", "supplier " + id);
    }

    public long addContact(long supplierId, ContactRequest request) {
        get(supplierId);
        String name = Html.escape(request.name().trim());
        String note = Html.escape(request.note());
        return contacts.insert(supplierId, name, request.email(), request.role().name(), note);
    }

    public List<Contact> contacts(long supplierId) {
        get(supplierId);
        return contacts.forSupplier(supplierId);
    }

    public Contact contact(long contactId) {
        return contacts.find(contactId);
    }

    public List<Contact> aliases(long contactId) {
        Contact contact = contacts.find(contactId);
        String address = contact.email() == null ? "" : contact.email();
        int at = address.indexOf('@');
        String mailbox = at < 0 ? address : address.substring(0, at);
        return contacts.sharingMailbox(contact.id(), mailbox);
    }

    public void updateBankAccount(long supplierId, BankAccountRequest request) {
        Supplier supplier = get(supplierId);
        if ("BLOCKED".equals(supplier.status())) {
            throw new ConflictException("blocked suppliers cannot change bank details");
        }
        suppliers.saveBankAccount(supplierId, request.holder().trim(), request.bic(), request.ibanTail());
        events.publishEvent(new BankDetailsChanged(supplierId, request.holder().trim(), request.bic()));
    }

    public WebsiteCheck checkWebsite(long id) {
        Supplier supplier = get(id);
        if (supplier.website() == null || supplier.website().isBlank()) {
            throw new InvalidRequestException("supplier has no website on file");
        }
        int bytes = probe.reach(supplier.website());
        return new WebsiteCheck(id, bytes >= 0, Math.max(bytes, 0));
    }

    public long addCertificate(long supplierId, CertificateRequest request) {
        get(supplierId);
        String fileName = request.fileName().trim();
        if (fileName.startsWith("/") || fileName.startsWith("..")) {
            throw new InvalidRequestException("certificate files live inside the certificate folder");
        }
        return certificates.insert(supplierId, request.issuer().trim(), fileName);
    }

    public byte[] certificateFile(long supplierId, long certificateId) throws IOException {
        Certificate certificate = certificates.find(supplierId, certificateId);
        return certificateFiles.content(certificate);
    }
}
