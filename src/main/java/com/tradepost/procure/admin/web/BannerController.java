package com.tradepost.procure.admin.web;

import com.tradepost.procure.admin.service.SettingsService;
import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.common.Redirector;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BannerController {

    private final SettingsService settings;
    private final PageWriter writer;
    private final Redirector redirector;

    public BannerController(SettingsService settings, PageWriter writer, Redirector redirector) {
        this.settings = settings;
        this.writer = writer;
        this.redirector = redirector;
    }

    @GetMapping("/portal/banner")
    public void banner(HttpServletResponse response) throws IOException {
        String body = "<div class=\"banner\">" + settings.banner() + "</div>";
        writer.html(response, writer.page("Notice", body));
    }

    @GetMapping("/portal/help")
    public void help(HttpServletResponse response) throws IOException {
        redirector.to(response, settings.helpUrl());
    }
}
