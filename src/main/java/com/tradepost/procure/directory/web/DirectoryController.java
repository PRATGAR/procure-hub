package com.tradepost.procure.directory.web;

import com.tradepost.procure.common.Redirector;
import com.tradepost.procure.directory.service.DirectoryService;
import com.tradepost.procure.directory.web.DirectoryDtos.ResolveRequest;
import com.tradepost.procure.directory.web.DirectoryDtos.SsoLinkRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final DirectoryService service;
    private final Redirector redirector;

    public DirectoryController(DirectoryService service, Redirector redirector) {
        this.service = service;
        this.redirector = redirector;
    }

    @GetMapping("/users")
    public List<Map<String, String>> find(@RequestParam String uid) throws NamingException {
        return service.findUser(uid);
    }

    @GetMapping("/users/search")
    public List<Map<String, String>> search(@RequestParam String name) throws NamingException {
        return service.searchUsers(name);
    }

    @GetMapping("/users/{uid}/groups")
    public List<Map<String, String>> groups(@PathVariable Uid uid) throws NamingException {
        return service.groupsOf(uid.value());
    }

    @GetMapping("/units/members")
    public List<Map<String, String>> members() throws NamingException {
        return service.unitMembers();
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

    @PostMapping("/sso/providers/resolve")
    public Map<String, Object> resolve(@Valid @RequestBody ResolveRequest request) throws NamingException {
        return service.resolveProvider(request.linkId());
    }

    @GetMapping("/sso/callback")
    public void callback(@RequestParam String returnTo, HttpServletResponse response) throws IOException {
        redirector.to(response, service.callbackTarget(returnTo));
    }
}
