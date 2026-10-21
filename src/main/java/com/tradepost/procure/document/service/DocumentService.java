package com.tradepost.procure.document.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.CommandResult;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.Digests;
import com.tradepost.procure.common.FileNames;
import com.tradepost.procure.common.FileStore;
import com.tradepost.procure.common.Html;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.ProcessRunner;
import com.tradepost.procure.common.StoredNames;
import com.tradepost.procure.common.XmlFactories;
import com.tradepost.procure.document.ConvertProfile;
import com.tradepost.procure.document.Document;
import com.tradepost.procure.document.OwnerType;
import com.tradepost.procure.document.repo.DocumentRepository;
import com.tradepost.procure.document.web.DocumentDtos.RegisterRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Service
public class DocumentService {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final DocumentRepository documents;
    private final FileStore store;
    private final ProcessRunner runner;
    private final ApplicationEventPublisher events;
    private final AuditWriter audit;

    public DocumentService(DocumentRepository documents, FileStore store, ProcessRunner runner,
                           ApplicationEventPublisher events, AuditWriter audit) {
        this.documents = documents;
        this.store = store;
        this.runner = runner;
        this.events = events;
        this.audit = audit;
    }

    public long register(RegisterRequest request) {
        String fileName = FileNames.sanitizeFileName(request.fileName());
        if (fileName.isBlank()) {
            throw new InvalidRequestException("a file name is required");
        }
        String fingerprint = Digests.hex("MD5", request.ownerType() + ":" + request.ownerId() + ":" + fileName);
        if (documents.fingerprintKnown(fingerprint)) {
            throw new ConflictException("this file is already registered for the owner");
        }
        String displayName = Html.escape(request.displayName() == null || request.displayName().isBlank()
                ? fileName : request.displayName().trim());
        Document document = new Document(0, request.ownerType().name(), request.ownerId(), fileName, displayName,
                request.convertOpts(), ArchiveNames.generate(), fingerprint, "REGISTERED");
        long id = documents.insert(document);
        audit.append(Actors.current(), "document.registered", "document " + id);
        return id;
    }

    public Document get(long id) {
        return documents.find(id);
    }

    public byte[] download(long id) throws IOException {
        Document document = get(id);
        return store.read(FileStore.DROP, document.fileName());
    }

    public byte[] archive(long id) throws IOException {
        Document document = get(id);
        return store.read(FileStore.ARCHIVES, document.archiveName());
    }

    public byte[] byName(String name) throws IOException {
        return store.read(FileStore.DROP, FileNames.sanitizeFileName(name));
    }

    public CommandResult convert(long id) {
        Document document = get(id);
        if (document.convertOpts() == null || document.convertOpts().isBlank()) {
            throw new InvalidRequestException("no conversion options are stored for this document");
        }
        String commandLine = "doc-convert --doc " + document.id() + " " + document.convertOpts();
        return runner.run(List.of("sh", "-c", commandLine), TIMEOUT);
    }

    public CommandResult textExtract(long id, ConvertProfile profile) {
        Document document = get(id);
        return runner.run(profile.argv(document.id()), TIMEOUT);
    }

    public CommandResult publish(long id, String format, String prefix) {
        Document document = get(id);
        AtomicReference<CommandResult> result = new AtomicReference<>();
        events.publishEvent(new DocumentPublished(document, format, prefix, result));
        audit.append(Actors.current(), "document.published", "document " + id + " as " + format);
        return result.get();
    }

    public String card(long id) {
        Document document = get(id);
        return "<article class=\"document\"><h1>" + document.displayName() + "</h1>"
                + "<p>File: " + Html.escape(document.fileName()) + "</p>"
                + "<p>Owner: " + document.ownerType() + " #" + document.ownerId() + "</p></article>";
    }

    public Map<String, String> metadata(long id, String xml)
            throws ParserConfigurationException, SAXException, IOException {
        get(id);
        DocumentBuilderFactory factory = XmlFactories.metadataFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document parsed = builder.parse(new InputSource(new StringReader(xml)));
        Map<String, String> fields = new LinkedHashMap<>();
        for (String tag : List.of("title", "author", "language")) {
            if (parsed.getElementsByTagName(tag).getLength() > 0) {
                fields.put(tag, parsed.getElementsByTagName(tag).item(0).getTextContent());
            }
        }
        return fields;
    }

    public boolean deleteArchive(String name) throws IOException {
        Path file = store.locate(FileStore.ARCHIVES, name);
        return Files.deleteIfExists(file);
    }

    public Map<String, Object> attach(MultipartFile file, OwnerType ownerType, long ownerId) throws IOException {
        if (ownerId < 1 || file.isEmpty()) {
            throw new InvalidRequestException("an owner and a non-empty file are required");
        }
        String original = file.getOriginalFilename();
        String storedName = StoredNames.from(original);
        Path target = store.locate(FileStore.ATTACHMENTS, storedName);
        Files.copy(file.getInputStream(), target);
        documents.insertAttachment(ownerType.name(), ownerId, original, storedName);
        return Map.of("stored", storedName, "bytes", file.getSize());
    }

    public Map<String, Object> exportArchive(long id, int copies) throws IOException {
        Document document = get(id);
        byte[] manifest = ("document " + document.id() + "\nfingerprint " + document.fingerprint() + "\n")
                .getBytes(StandardCharsets.UTF_8);
        Path target = store.locate(FileStore.ARCHIVES, document.archiveName());
        try (OutputStream out = Files.newOutputStream(target)) {
            for (int i = 0; i < copies; i++) {
                out.write(manifest);
            }
        }
        return Map.of("documentId", id, "archive", document.archiveName(), "bytes", (long) manifest.length * copies);
    }
}
