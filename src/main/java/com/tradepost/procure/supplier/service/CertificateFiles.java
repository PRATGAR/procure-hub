package com.tradepost.procure.supplier.service;

import com.tradepost.procure.common.FileStore;
import com.tradepost.procure.supplier.Certificate;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class CertificateFiles {

    private final FileStore store;

    public CertificateFiles(FileStore store) {
        this.store = store;
    }

    public byte[] content(Certificate certificate) throws IOException {
        return store.read(FileStore.CERTIFICATES, certificate.fileName());
    }
}
