package com.tradepost.procure.shipment.web;

import com.tradepost.procure.shipment.Carrier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ShipmentDtos {

    private ShipmentDtos() {
    }

    public record CreateShipmentRequest(
            @Min(1) long orderId,
            @NotNull Carrier carrier,
            @NotBlank @Pattern(regexp = "^[A-Z0-9]{8,24}$") String trackingNo,
            @Size(max = 300) String notes) {
    }

    public record CarrierEvent(
            @NotBlank @Size(max = 30) String trackingNo,
            @NotBlank @Size(max = 120) String status,
            @Size(max = 300) String detail) {
    }

    public record LabelFile(String name, byte[] content) {
    }
}
