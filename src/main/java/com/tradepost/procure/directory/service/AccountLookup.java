package com.tradepost.procure.directory.service;

import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import org.springframework.stereotype.Component;

@Component
public class AccountLookup {

    private final DirectoryClient client;

    public AccountLookup(DirectoryClient client) {
        this.client = client;
    }

    public List<Map<String, String>> byLogin(String login) throws NamingException {
        String filter = "(&(objectClass=person)(uid=" + login + "))";
        return client.findByFilter(filter);
    }
}
