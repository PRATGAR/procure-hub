package com.tradepost.procure.shipment.service;

import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.shipment.Shipment;
import com.tradepost.procure.shipment.ShipmentStatus;
import com.tradepost.procure.shipment.repo.ShipmentRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class ShipmentNotifier {

    private static final Logger log = LoggerFactory.getLogger(ShipmentNotifier.class);

    private final ShipmentRepository shipments;
    private final OutboundHttp outbound;

    public ShipmentNotifier(ShipmentRepository shipments, OutboundHttp outbound) {
        this.shipments = shipments;
        this.outbound = outbound;
    }

    public void statusChanged(Shipment shipment, ShipmentStatus status) {
        String target = shipments.notifyUrl(shipment.id());
        if (target == null || target.isBlank()) {
            return;
        }
        try {
            outbound.deliver(target, Map.of("shipmentId", shipment.id(), "status", status.name()));
        } catch (RestClientException e) {
            log.warn("status notice for shipment {} was not delivered", shipment.id());
        }
    }
}
