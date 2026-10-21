package com.tradepost.procure.order.web;

import com.tradepost.procure.common.Redirector;
import com.tradepost.procure.order.OrderField;
import com.tradepost.procure.order.service.OrderService;
import com.tradepost.procure.order.web.OrderDtos.ApproveRequest;
import com.tradepost.procure.order.web.OrderDtos.CreateOrderRequest;
import com.tradepost.procure.order.web.OrderDtos.HookRequest;
import com.tradepost.procure.order.web.OrderDtos.OrderView;
import com.tradepost.procure.order.web.OrderDtos.ReceiveRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;
    private final Redirector redirector;

    public OrderController(OrderService service, Redirector redirector) {
        this.service = service;
        this.redirector = redirector;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateOrderRequest request) {
        long id = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "DRAFT"));
    }

    @GetMapping("/{id}")
    public OrderView get(@PathVariable long id) {
        return service.view(id);
    }

    @PutMapping("/{id}/delivery-hook")
    public Map<String, Object> updateHook(@PathVariable long id, @Valid @RequestBody HookRequest request) {
        service.updateHook(id, request.deliveryHook());
        return Map.of("orderId", id, "updated", true);
    }

    @PostMapping("/{id}/submit")
    public Map<String, Object> submit(@PathVariable long id) {
        service.submit(id);
        return Map.of("orderId", id, "status", "SUBMITTED");
    }

    @PostMapping("/{id}/approve")
    public Map<String, Object> approve(@PathVariable long id, @Valid @RequestBody ApproveRequest request,
                                       HttpServletResponse response) throws IOException {
        String returnUrl = service.approve(id, request.approvalCode());
        if (returnUrl != null && !returnUrl.isBlank()) {
            redirector.to(response, returnUrl);
            return null;
        }
        return Map.of("orderId", id, "status", "APPROVED");
    }

    @GetMapping("/{id}/approve-link")
    public void approveLink(@PathVariable long id, @RequestParam(required = false) String returnTo,
                            HttpServletResponse response) throws IOException {
        String target = service.approvalLandingTarget(id, returnTo);
        redirector.to(response, target);
    }

    @PostMapping("/{id}/send")
    public Map<String, Object> send(@PathVariable long id) {
        return service.send(id);
    }

    @PostMapping("/{id}/receive")
    public Map<String, Object> receive(@PathVariable long id, @Valid @RequestBody ReceiveRequest request) {
        return service.receive(id, request);
    }

    @GetMapping("/{id}/po")
    public Map<String, Object> po(@PathVariable long id) {
        return Map.of("orderId", id, "text", service.poText(id));
    }

    @GetMapping("/{id}/label-text")
    public Map<String, Object> labelText(@PathVariable long id) {
        return Map.of("orderId", id, "label", service.labelText(id));
    }

    @GetMapping("/{id}/fields/{field}")
    public Map<String, Object> field(@PathVariable long id, @PathVariable OrderField field) {
        Object value = service.field(id, field);
        return Map.of("orderId", id, "field", field.name(), "value", String.valueOf(value));
    }
}
