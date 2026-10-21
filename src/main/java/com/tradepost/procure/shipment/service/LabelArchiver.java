package com.tradepost.procure.shipment.service;

import com.tradepost.procure.common.FileStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class LabelArchiver {

    private final FileStore store;

    public LabelArchiver(FileStore store) {
        this.store = store;
    }

    public void archive(ShipmentStatusChanged event) throws IOException {
        String name = "shipment-" + event.shipmentId() + "-" + event.status().name().toLowerCase(Locale.ROOT) + ".txt";
        byte[] content = ("status " + event.status().name() + " for shipment " + event.shipmentId())
                .getBytes(StandardCharsets.UTF_8);
        store.write(FileStore.ARCHIVES, name, content);
    }
}
