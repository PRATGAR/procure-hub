package com.tradepost.procure.shipment.service;

final class LabelNames {

    private LabelNames() {
    }

    static String forShipment(long shipmentId) {
        return "LBL-" + shipmentId + ".txt";
    }
}
