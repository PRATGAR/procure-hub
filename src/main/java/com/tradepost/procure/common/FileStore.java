package com.tradepost.procure.common;

import com.tradepost.procure.config.TradepostProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FileStore {

    public static final String MEDIA = "media";
    public static final String THUMBS = "thumbs";
    public static final String DROP = "drop";
    public static final String ARCHIVES = "archives";
    public static final String EXPORTS = "exports";
    public static final String LABELS = "labels";
    public static final String ATTACHMENTS = "attachments";
    public static final String PDF = "pdf";

    private static final List<String> AREAS =
            List.of(MEDIA, THUMBS, DROP, ARCHIVES, EXPORTS, LABELS, ATTACHMENTS, PDF);

    private final Path root;

    public FileStore(TradepostProperties properties) {
        this.root = Path.of(properties.storage().root()).toAbsolutePath().normalize();
    }

    @PostConstruct
    void prepareAreas() throws IOException {
        for (String area : AREAS) {
            Files.createDirectories(root.resolve(area));
        }
        Path invoiceTemplate = root.resolve(PDF).resolve("invoice.txt");
        if (Files.notExists(invoiceTemplate)) {
            Files.writeString(invoiceTemplate, "INVOICE {{number}}\nReference: {{reference}}\n");
        }
        Path sampleNote = root.resolve(DROP).resolve("delivery-note-7781.txt");
        if (Files.notExists(sampleNote)) {
            Files.writeString(sampleNote, "DELIVERY NOTE 7781");
        }
        Path sampleExport = root.resolve(EXPORTS).resolve("report-1-1.csv");
        if (Files.notExists(sampleExport)) {
            Files.writeString(sampleExport, "id,number,status");
        }
        Path sampleLabel = root.resolve(LABELS).resolve("LBL-1.txt");
        if (Files.notExists(sampleLabel)) {
            Files.writeString(sampleLabel, "SHIPPING LABEL\nShipment 1\nHandle with care\n");
        }
    }

    public Path root() {
        return root;
    }

    public Path locate(String area, String name) {
        return root.resolve(area).resolve(name);
    }

    public byte[] read(String area, String name) throws IOException {
        Path file = locate(area, name);
        return Files.readAllBytes(file);
    }

    public void write(String area, String name, byte[] content) throws IOException {
        Path file = locate(area, name);
        Files.write(file, content);
    }
}
