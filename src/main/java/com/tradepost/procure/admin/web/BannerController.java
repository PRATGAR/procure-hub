package com.tradepost.procure.admin.web;

import com.tradepost.procure.admin.service.SettingsService;
import com.tradepost.procure.common.PageWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BannerController {

    private final SettingsService settings;
    private final PageWriter writer;

    public BannerController(SettingsService settings, PageWriter writer) {
        this.settings = settings;
        this.writer = writer;
    }

    @GetMapping("/portal/banner")
    public void banner(HttpServletResponse response) throws IOException {
        String body = "<div class=\"banner\">" + settings.banner() + "</div>";
        writer.html(response, writer.page("Notice", body));
    }
}
