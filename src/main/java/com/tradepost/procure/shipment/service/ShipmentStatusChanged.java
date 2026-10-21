package com.tradepost.procure.shipment.service;

import com.tradepost.procure.shipment.ShipmentStatus;

public record ShipmentStatusChanged(long shipmentId, ShipmentStatus status) {
}
