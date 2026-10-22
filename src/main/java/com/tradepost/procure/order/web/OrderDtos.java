package com.tradepost.procure.order.web;

import com.tradepost.procure.order.Order;
import com.tradepost.procure.order.OrderLine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record LineRequest(
            @NotBlank @Pattern(regexp = "^[A-Z0-9-]{4,24}$") String sku,
            @Min(1) int qty,
            @Min(1) long unitCents) {
    }

    public record CreateOrderRequest(
            @Min(1) long supplierId,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
            @NotEmpty @Valid List<LineRequest> lines,
            @Size(max = 200) String shipTo,
            @Size(max = 1000) String instructions,
            @Size(max = 300) String labelTemplate,
            @Pattern(regexp = "^/[A-Za-z0-9_/.-]{0,119}$") String returnUrl,
            @Size(max = 300) String deliveryHook) {
    }

    public record HookRequest(@Size(max = 300) String deliveryHook) {
    }

    public record ApproveRequest(@NotBlank @Pattern(regexp = "^[0-9]{6}$") String approvalCode) {
    }

    public record ReceiveRequest(@Min(1) long lineId, @Min(1) int qty) {
    }

    public record OrderView(Order order, List<OrderLine> lines) {
    }
}
