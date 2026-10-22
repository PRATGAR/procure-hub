package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.FileStore;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class ContractFiles {

    private final FileStore store;

    public ContractFiles(FileStore store) {
        this.store = store;
    }

    public byte[] open(String fileName) throws IOException {
        return store.read(FileStore.DROP, fileName);
    }
}
