package com.tradepost.procure.rfq.service;

import com.tradepost.procure.common.Html;
import com.tradepost.procure.common.PageWriter;
import com.tradepost.procure.rfq.Question;
import com.tradepost.procure.rfq.Rfq;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RfqPages {

    private final PageWriter writer;

    public RfqPages(PageWriter writer) {
        this.writer = writer;
    }

    public String questions(Rfq rfq, List<Question> questions) {
        StringBuilder items = new StringBuilder();
        for (Question question : questions) {
            items.append("<li><p class=\"question\">").append(question.question()).append("</p>");
            if (question.answer() != null) {
                items.append("<p class=\"answer\">").append(Html.escape(question.answer())).append("</p>");
            }
            items.append("</li>");
        }
        String heading = Html.escape(rfq.title());
        return writer.page("Questions", "<h1>" + heading + "</h1><ol>" + items + "</ol>");
    }
}
