package com.tradepost.procure.common;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class Redirector {

    public void to(HttpServletResponse response, String target) throws IOException {
        response.sendRedirect(target);
    }
}
