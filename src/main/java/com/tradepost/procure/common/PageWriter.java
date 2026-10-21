package com.tradepost.procure.common;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.stereotype.Component;

@Component
public class PageWriter {

    public String page(String title, String body) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>" + title
                + "</title></head><body>" + body + "</body></html>";
    }

    public void html(HttpServletResponse response, String body) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(body);
    }
}
