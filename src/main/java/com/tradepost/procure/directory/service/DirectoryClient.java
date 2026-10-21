package com.tradepost.procure.directory.service;

import com.tradepost.procure.config.TradepostProperties;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.springframework.stereotype.Component;

@Component
public class DirectoryClient {

    private static final int MAX_RESULTS = 50;

    private final TradepostProperties properties;

    public DirectoryClient(TradepostProperties properties) {
        this.properties = properties;
    }

    public List<Map<String, String>> findByFilter(String filter) throws NamingException {
        DirContext ctx = open();
        try {
            NamingEnumeration<SearchResult> results = ctx.search(properties.directory().baseDn(), filter, controls());
            return collect(results);
        } finally {
            ctx.close();
        }
    }

    public List<Map<String, String>> listUnit(String unit) throws NamingException {
        DirContext ctx = open();
        try {
            NamingEnumeration<SearchResult> results = ctx.search(properties.directory().baseDn(), "(ou=" + unit + ")", controls());
            return collect(results);
        } finally {
            ctx.close();
        }
    }

    public Object lookupProvider(String name) throws NamingException {
        Context ctx = new InitialContext();
        try {
            return ctx.lookup(name);
        } finally {
            ctx.close();
        }
    }

    private DirContext open() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, properties.directory().url());
        env.put("com.sun.jndi.ldap.connect.timeout", "2000");
        env.put("com.sun.jndi.ldap.read.timeout", "3000");
        return new InitialDirContext(env);
    }

    private SearchControls controls() {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setCountLimit(MAX_RESULTS);
        controls.setReturningAttributes(new String[] {"uid", "cn", "mail", "ou"});
        return controls;
    }

    private List<Map<String, String>> collect(NamingEnumeration<SearchResult> results) throws NamingException {
        List<Map<String, String>> entries = new ArrayList<>();
        while (results.hasMore()) {
            SearchResult result = results.next();
            Attributes attributes = result.getAttributes();
            Map<String, String> entry = new java.util.LinkedHashMap<>();
            for (String name : List.of("uid", "cn", "mail", "ou")) {
                if (attributes.get(name) != null) {
                    entry.put(name, String.valueOf(attributes.get(name).get()));
                }
            }
            entries.add(entry);
        }
        return entries;
    }
}
