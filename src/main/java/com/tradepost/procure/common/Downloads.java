package com.tradepost.procure.common;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public final class Downloads {

    private Downloads() {
    }

    public static PrintWriter csv(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("X-Content-Type-Options", "nosniff");
        return response.getWriter();
    }
}
