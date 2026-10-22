package com.tradepost.procure.directory.web;

import com.tradepost.procure.directory.service.DirectoryService;
import com.tradepost.procure.directory.web.DirectoryDtos.MetadataFetched;
import com.tradepost.procure.directory.web.DirectoryDtos.ResolveRequest;
import com.tradepost.procure.directory.web.DirectoryDtos.SsoLinkRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final DirectoryService service;

    public DirectoryController(DirectoryService service) {
        this.service = service;
    }

    @GetMapping("/contacts/{contactId}/entry")
    public List<Map<String, String>> contactEntry(@PathVariable long contactId) throws NamingException {
        return service.contactEntry(contactId);
    }

    @GetMapping("/suppliers/{supplierId}/accounts")
    public List<Map<String, String>> supplierAccounts(@PathVariable long supplierId) throws NamingException {
        return service.supplierAccounts(supplierId);
    }

    @PostMapping("/sso/links")
    public ResponseEntity<Map<String, Object>> link(@Valid @RequestBody SsoLinkRequest request) {
        long id = service.link(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @GetMapping("/sso/links/{id}/verify")
    public Map<String, Object> verify(@PathVariable long id) throws NamingException {
        return service.verify(id);
    }

    @GetMapping("/sso/links/{id}/team-members")
    public List<Map<String, String>> teamMembers(@PathVariable long id) throws NamingException {
        return service.teamMembers(id);
    }

    @GetMapping("/sso/links/{id}/metadata")
    public MetadataFetched metadata(@PathVariable long id) {
        return service.fetchMetadata(id);
    }

    @PostMapping("/sso/providers/resolve")
    public Map<String, Object> resolve(@Valid @RequestBody ResolveRequest request) throws NamingException {
        return service.resolveProvider(request.linkId());
    }
}
