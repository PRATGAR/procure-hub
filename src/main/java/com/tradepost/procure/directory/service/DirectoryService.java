package com.tradepost.procure.directory.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.HostAllowlist;
import com.tradepost.procure.common.NotFoundException;
import com.tradepost.procure.common.RequestContext;
import com.tradepost.procure.common.Terms;
import com.tradepost.procure.config.TradepostProperties;
import com.tradepost.procure.directory.SsoLink;
import com.tradepost.procure.directory.repo.SsoLinkRepository;
import com.tradepost.procure.directory.web.DirectoryDtos.SsoLinkRequest;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import org.springframework.stereotype.Service;

@Service
public class DirectoryService {

    private final DirectoryClient client;
    private final SsoLinkRepository links;
    private final AuditWriter audit;
    private final TradepostProperties properties;

    public DirectoryService(DirectoryClient client, SsoLinkRepository links, AuditWriter audit,
                            TradepostProperties properties) {
        this.client = client;
        this.links = links;
        this.audit = audit;
        this.properties = properties;
    }

    public List<Map<String, String>> findUser(String uid) throws NamingException {
        String filter = "(&(objectClass=person)(uid=" + uid + "))";
        return client.findByFilter(filter);
    }

    public List<Map<String, String>> searchUsers(String name) throws NamingException {
        String filter = "(&(objectClass=person)(cn=*" + Terms.escapeFilter(name) + "*))";
        return client.findByFilter(filter);
    }

    public List<Map<String, String>> groupsOf(String uid) throws NamingException {
        String filter = "(&(objectClass=groupOfNames)(member=uid=" + uid + ",ou=people))";
        return client.findByFilter(filter);
    }

    public List<Map<String, String>> unitMembers() throws NamingException {
        return client.listUnit(RequestContext.unit());
    }

    public long link(SsoLinkRequest request) {
        links.insert(request.userRef(), request.provider(), request.externalId().trim(), request.unit());
        audit.append(Actors.current(), "sso.linked", "user " + request.userRef());
        return links.lastIdFor(request.userRef());
    }

    public Map<String, Object> verify(long id) throws NamingException {
        SsoLink link = requireLink(id);
        String filter = "(&(objectClass=person)(uid=" + link.externalId() + "))";
        List<Map<String, String>> accounts = client.findByFilter(filter);
        List<Map<String, String>> unitPeers = client.listUnit(link.unit());
        return Map.of("linkId", id, "accounts", accounts, "unitPeers", unitPeers.size());
    }

    public Map<String, Object> resolveProvider(long linkId) throws NamingException {
        SsoLink link = requireLink(linkId);
        Object reference = client.lookupProvider("java:comp/env/sso/" + link.provider());
        return Map.of("linkId", linkId, "provider", link.provider(), "bound", reference != null);
    }

    public String callbackTarget(String returnTo) {
        if (HostAllowlist.allows(returnTo, properties.trustedReturnHosts())) {
            return returnTo;
        }
        return "/api/directory/units/members";
    }

    private SsoLink requireLink(long id) {
        SsoLink link = links.find(id);
        if (link == null) {
            throw new NotFoundException("sso link", id);
        }
        return link;
    }
}
