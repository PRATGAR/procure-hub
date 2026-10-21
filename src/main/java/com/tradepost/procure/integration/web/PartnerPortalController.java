package com.tradepost.procure.integration.web;

import com.tradepost.procure.common.Redirector;
import com.tradepost.procure.integration.Partner;
import com.tradepost.procure.integration.PartnerCode;
import com.tradepost.procure.integration.service.IntegrationService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/partner")
public class PartnerPortalController {

    private final IntegrationService service;
    private final Redirector redirector;

    public PartnerPortalController(IntegrationService service, Redirector redirector) {
        this.service = service;
        this.redirector = redirector;
    }

    @GetMapping("/go/{code}")
    public void go(@PathVariable PartnerCode code, @RequestParam(defaultValue = "portal") String to,
                   HttpServletResponse response) throws IOException {
        if ("docs".equals(to)) {
            response.sendRedirect(service.docsUrl(code));
            return;
        }
        Partner partner = service.partnerByCode(code);
        redirector.to(response, partner.portalUrl());
    }
}
