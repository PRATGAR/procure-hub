package com.tradepost.procure.common;

import java.util.List;

public final class ShellCommands {

    private ShellCommands() {
    }

    public static List<String> pdfRender(long documentId, String prefix) {
        return List.of("sh", "-c", "pdf-render --doc " + documentId + " --prefix " + prefix);
    }

    public static List<String> reportPdf(long reportId, String title) {
        return List.of("sh", "-c", "report-pdf --report " + reportId + " --title '" + title + "'");
    }

    public static List<String> ping(String host) {
        return List.of("sh", "-c", "ping -c 1 " + host);
    }
}
