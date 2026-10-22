package com.tradepost.procure.order.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.Codes;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.EvalContexts;
import com.tradepost.procure.common.ExpressionService;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.config.TradepostProperties;
import com.tradepost.procure.order.Order;
import com.tradepost.procure.order.OrderFacts;
import com.tradepost.procure.order.OrderField;
import com.tradepost.procure.order.OrderLine;
import com.tradepost.procure.order.repo.OrderRepository;
import com.tradepost.procure.order.web.OrderDtos.CreateOrderRequest;
import com.tradepost.procure.order.web.OrderDtos.LineRequest;
import com.tradepost.procure.order.web.OrderDtos.OrderView;
import com.tradepost.procure.order.web.OrderDtos.ReceiveRequest;
import com.tradepost.procure.supplier.Supplier;
import com.tradepost.procure.supplier.service.SupplierService;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class OrderService {

    private static final int SEND_ATTEMPTS = 2;

    private final OrderRepository orders;
    private final SupplierService suppliers;
    private final HookPolicy hookPolicy;
    private final OutboundHttp outbound;
    private final ExpressionService expressions;
    private final ApplicationEventPublisher events;
    private final AuditWriter audit;
    private final TradepostProperties properties;
    private final Random approvalCodes;

    public OrderService(OrderRepository orders, SupplierService suppliers, HookPolicy hookPolicy,
                        OutboundHttp outbound, ExpressionService expressions, ApplicationEventPublisher events,
                        AuditWriter audit, TradepostProperties properties,
                        @Qualifier("approvalCodes") Random approvalCodes) {
        this.orders = orders;
        this.suppliers = suppliers;
        this.hookPolicy = hookPolicy;
        this.outbound = outbound;
        this.expressions = expressions;
        this.events = events;
        this.audit = audit;
        this.properties = properties;
        this.approvalCodes = approvalCodes;
    }

    public long create(CreateOrderRequest request) {
        suppliers.get(request.supplierId());
        long total = request.lines().stream().mapToLong(line -> line.qty() * line.unitCents()).sum();
        String poNumber = "PO-" + (1001 + orders.count());
        Order draft = new Order(0, poNumber, request.supplierId(), "DRAFT", total, request.currency(),
                request.instructions(), request.labelTemplate(), request.returnUrl(), request.deliveryHook(),
                null, request.shipTo(), Actors.current());
        long id = orders.insert(draft);
        for (LineRequest line : request.lines()) {
            orders.insertLine(id, line.sku(), line.qty(), line.unitCents());
        }
        audit.append(Actors.current(), "order.created", poNumber);
        return id;
    }

    public OrderView view(long id) {
        return new OrderView(orders.find(id), orders.lines(id));
    }

    public Order get(long id) {
        return orders.find(id);
    }

    public void updateHook(long id, String hook) {
        Order order = get(id);
        if ("SENT".equals(order.status()) || "RECEIVED".equals(order.status())) {
            throw new ConflictException("the order has already left the buyer");
        }
        orders.updateHook(id, hook);
        audit.append(Actors.current(), "order.hook.updated", order.poNumber());
    }

    public void submit(long id) {
        Order order = get(id);
        if (!"DRAFT".equals(order.status())) {
            throw new ConflictException("only draft orders can be submitted");
        }
        if (order.deliveryHook() != null && !order.deliveryHook().isBlank()) {
            hookPolicy.require(order.deliveryHook());
        }
        String code = Codes.digits(approvalCodes, 6);
        orders.markSubmitted(id, code);
        audit.append(Actors.current(), "order.submitted", order.poNumber());
    }

    public String approve(long id, String approvalCode) {
        Order order = get(id);
        if (!"SUBMITTED".equals(order.status())) {
            throw new ConflictException("only submitted orders can be approved");
        }
        if (!approvalCode.equals(order.approvalCode())) {
            throw new InvalidRequestException("approval code does not match");
        }
        orders.updateStatus(id, "APPROVED");
        audit.append(Actors.current(), "order.approved", order.poNumber());
        events.publishEvent(new OrderApproved(id, order.supplierId(), order.poNumber(), order.totalCents()));
        return order.returnUrl();
    }

    public String approvalLandingTarget(long id) {
        Order order = get(id);
        return order.returnUrl() == null ? "/api/orders/" + id : order.returnUrl();
    }

    public void updateReturnUrl(long id, String returnUrl) {
        Order order = get(id);
        orders.updateReturnUrl(id, returnUrl);
        audit.append(Actors.current(), "order.return.updated", order.poNumber());
    }

    public Map<String, Object> send(long id) {
        Order order = get(id);
        if (!"APPROVED".equals(order.status())) {
            throw new ConflictException("the order must be approved before it is sent");
        }
        String hook = order.deliveryHook() == null || order.deliveryHook().isBlank()
                ? properties.dispatchUrls().get("webhook") : order.deliveryHook();
        Map<String, Object> payload = Map.of("poNumber", order.poNumber(), "totalCents", order.totalCents(),
                "currency", order.currency());
        for (int attempt = 1; attempt <= SEND_ATTEMPTS; attempt++) {
            try {
                outbound.deliver(hook, payload);
                orders.updateStatus(id, "SENT");
                int serial = ThreadLocalRandom.current().nextInt(100000, 1000000);
                events.publishEvent(new OrderSent(id, order.poNumber(), serial));
                return Map.of("orderId", id, "status", "SENT", "attempts", attempt);
            } catch (RestClientException e) {
                if (attempt == SEND_ATTEMPTS) {
                    throw e;
                }
                pause(150);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    public Map<String, Object> receive(long id, ReceiveRequest request) {
        Order order = get(id);
        if (!"SENT".equals(order.status())) {
            throw new ConflictException("goods can only be received for sent orders");
        }
        int touched = orders.receive(id, request.lineId(), request.qty());
        if (touched == 0) {
            throw new InvalidRequestException("unknown order line");
        }
        List<OrderLine> lines = orders.lines(id);
        boolean complete = lines.stream().allMatch(line -> line.receivedQty() >= line.qty());
        if (complete) {
            orders.updateStatus(id, "RECEIVED");
        }
        return Map.of("orderId", id, "complete", complete);
    }

    public String poText(long id) {
        Order order = get(id);
        String instructions = order.instructions() == null ? "" : order.instructions();
        OrderFacts facts = facts(order);
        String rendered = expressions.render(instructions, EvalContexts.standard(Map.of("order", facts)), facts);
        return "PURCHASE ORDER " + order.poNumber() + "\n" + rendered;
    }

    public String labelText(long id) {
        Order order = get(id);
        String template = order.labelTemplate() == null ? "" : order.labelTemplate();
        return expressions.render(template, EvalContexts.simple(), facts(order));
    }

    public Object field(long id, OrderField field) {
        Order order = get(id);
        return expressions.evaluate(field.path(), EvalContexts.simple(), facts(order));
    }

    private OrderFacts facts(Order order) {
        Supplier supplier = suppliers.get(order.supplierId());
        return new OrderFacts(order.poNumber(), order.totalCents(), order.currency(), order.shipTo(),
                supplier.legalName(), order.status());
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
