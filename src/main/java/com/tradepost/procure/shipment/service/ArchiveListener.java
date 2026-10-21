package com.tradepost.procure.shipment.service;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ArchiveListener {

    private static final Logger log = LoggerFactory.getLogger(ArchiveListener.class);

    private final LabelArchiver archiver;

    public ArchiveListener(LabelArchiver archiver) {
        this.archiver = archiver;
    }

    @EventListener
    public void onShipmentStatusChanged(ShipmentStatusChanged event) {
        try {
            archiver.archive(event);
        } catch (IOException e) {
            log.warn("archive of shipment {} failed", event.shipmentId());
        }
    }
}
