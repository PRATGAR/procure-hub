package com.tradepost.procure.shipment.web;

import com.tradepost.procure.shipment.Carrier;
import com.tradepost.procure.shipment.Shipment;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public final class ShipmentDtos {

    private ShipmentDtos() {
    }

    public record CreateShipmentRequest(
            @Min(1) long orderId,
            @NotNull Carrier carrier,
            @Min(10000000) @Max(99999999) long serial,
            @Size(max = 300) String notes) {
    }

    public record NotifyRequest(@NotBlank @Size(max = 300) String url) {
    }

    public record Created(long id, String status) {
    }

    public record ShipmentDetail(Shipment shipment, List<Map<String, Object>> peersWithSameStatus,
                                 List<Map<String, Object>> events) {
    }

    public record CarrierLoad(String carrier, int openShipments) {
    }

    public record LabelRequested(long shipmentId, String label) {
    }

    public record TrackingRefresh(long shipmentId, String carrier, int bytes) {
    }

    public record CallbackApplied(long shipmentId, String status) {
    }

    public record NotifyUrlSaved(long shipmentId, boolean saved) {
    }

    public record CarrierEvent(
            @NotBlank @Size(max = 30) String trackingNo,
            @NotBlank @Size(max = 120) String status,
            @Size(max = 300) String detail) {
    }

    public record LabelFile(String name, byte[] content) {
    }
}
