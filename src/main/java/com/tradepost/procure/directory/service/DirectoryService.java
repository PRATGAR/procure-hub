package com.tradepost.procure.directory.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.NotFoundException;
import com.tradepost.procure.common.Terms;
import com.tradepost.procure.directory.SsoLink;
import com.tradepost.procure.directory.repo.SsoLinkRepository;
import com.tradepost.procure.directory.web.DirectoryDtos.MetadataFetched;
import com.tradepost.procure.directory.web.DirectoryDtos.SsoLinkRequest;
import com.tradepost.procure.supplier.Contact;
import com.tradepost.procure.supplier.Supplier;
import com.tradepost.procure.supplier.service.SupplierService;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import org.springframework.stereotype.Service;

@Service
public class DirectoryService {

    private final DirectoryClient client;
    private final AccountLookup accounts;
    private final IdpMetadata metadata;
    private final SupplierService suppliers;
    private final SsoLinkRepository links;
    private final AuditWriter audit;

    public DirectoryService(DirectoryClient client, AccountLookup accounts, IdpMetadata metadata,
                            SupplierService suppliers, SsoLinkRepository links, AuditWriter audit) {
        this.client = client;
        this.accounts = accounts;
        this.metadata = metadata;
        this.suppliers = suppliers;
        this.links = links;
        this.audit = audit;
    }

    public List<Map<String, String>> contactEntry(long contactId) throws NamingException {
        Contact contact = suppliers.contact(contactId);
        String filter = "(&(objectClass=person)(cn=" + contact.name() + "))";
        return client.findByFilter(filter);
    }

    public List<Map<String, String>> supplierAccounts(long supplierId) throws NamingException {
        Supplier supplier = suppliers.get(supplierId);
        String name = supplier.displayName() == null ? supplier.legalName() : supplier.displayName();
        String filter = "(&(objectClass=person)(o=*" + Terms.escapeFilter(name) + "*))";
        return client.findByFilter(filter);
    }

    public long link(SsoLinkRequest request) {
        links.insert(request.userRef(), request.provider(), request.externalId().trim(), request.unit(),
                request.team(), request.metadataUrl());
        audit.append(Actors.current(), "sso.linked", "user " + request.userRef());
        return links.lastIdFor(request.userRef());
    }

    public Map<String, Object> verify(long id) throws NamingException {
        SsoLink link = requireLink(id);
        List<Map<String, String>> found = accounts.byLogin(link.externalId());
        List<Map<String, String>> unitPeers = client.listUnit(link.unit());
        return Map.of("linkId", id, "accounts", found, "unitPeers", unitPeers.size());
    }

    public List<Map<String, String>> teamMembers(long id) throws NamingException {
        SsoLink link = requireLink(id);
        return client.listUnit(link.team() == null ? link.unit() : link.team());
    }

    public MetadataFetched fetchMetadata(long id) {
        SsoLink link = requireLink(id);
        if (link.metadataUrl() == null || link.metadataUrl().isBlank()) {
            throw new InvalidRequestException("the link has no provider metadata location");
        }
        return new MetadataFetched(id, metadata.fetch(link.metadataUrl()));
    }

    public Map<String, Object> resolveProvider(long linkId) throws NamingException {
        SsoLink link = requireLink(linkId);
        Object reference = client.lookupProvider("java:comp/env/sso/" + link.provider());
        return Map.of("linkId", linkId, "provider", link.provider(), "bound", reference != null);
    }

    private SsoLink requireLink(long id) {
        SsoLink link = links.find(id);
        if (link == null) {
            throw new NotFoundException("sso link", id);
        }
        return link;
    }
}
