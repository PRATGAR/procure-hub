package com.tradepost.procure.shipment.service;

import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.AuditWriter;
import com.tradepost.procure.common.ConflictException;
import com.tradepost.procure.common.FileStore;
import com.tradepost.procure.common.InvalidRequestException;
import com.tradepost.procure.common.NotFoundException;
import com.tradepost.procure.common.OutboundHttp;
import com.tradepost.procure.common.QueryFragments;
import com.tradepost.procure.common.SearchSupport;
import com.tradepost.procure.config.TradepostProperties;
import com.tradepost.procure.order.service.OrderService;
import com.tradepost.procure.shipment.Carrier;
import com.tradepost.procure.shipment.Shipment;
import com.tradepost.procure.shipment.ShipmentSort;
import com.tradepost.procure.shipment.ShipmentStatus;
import com.tradepost.procure.shipment.repo.ShipmentRepository;
import com.tradepost.procure.shipment.web.ShipmentDtos.CallbackApplied;
import com.tradepost.procure.shipment.web.ShipmentDtos.CarrierEvent;
import com.tradepost.procure.shipment.web.ShipmentDtos.CarrierLoad;
import com.tradepost.procure.shipment.web.ShipmentDtos.CreateShipmentRequest;
import com.tradepost.procure.shipment.web.ShipmentDtos.LabelFile;
import com.tradepost.procure.shipment.web.ShipmentDtos.LabelRequested;
import com.tradepost.procure.shipment.web.ShipmentDtos.NotifyUrlSaved;
import com.tradepost.procure.shipment.web.ShipmentDtos.ShipmentDetail;
import com.tradepost.procure.shipment.web.ShipmentDtos.TrackingRefresh;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

@Service
public class ShipmentService {

    private static final String LIST_SELECT = "SELECT id, order_id, carrier, tracking_no, status FROM shipments";

    private final ShipmentRepository shipments;
    private final OrderService orders;
    private final SearchSupport search;
    private final FileStore store;
    private final OutboundHttp outbound;
    private final ApplicationEventPublisher events;
    private final AuditWriter audit;
    private final TradepostProperties properties;
    private final ShipmentNotifier notifier;

    public ShipmentService(ShipmentRepository shipments, OrderService orders, SearchSupport search, FileStore store,
                           OutboundHttp outbound, ApplicationEventPublisher events, AuditWriter audit,
                           TradepostProperties properties, ShipmentNotifier notifier) {
        this.notifier = notifier;
        this.shipments = shipments;
        this.orders = orders;
        this.search = search;
        this.store = store;
        this.outbound = outbound;
        this.events = events;
        this.audit = audit;
        this.properties = properties;
    }

    public long create(CreateShipmentRequest request) {
        orders.get(request.orderId());
        String trackingNo = request.carrier().prefix() + request.serial();
        if (shipments.trackingTaken(trackingNo)) {
            throw new ConflictException("the tracking number is already registered");
        }
        long id = shipments.insert(request.orderId(), request.carrier().name(), trackingNo, request.notes());
        audit.append(Actors.current(), "shipment.created", "shipment " + id);
        return id;
    }

    public List<Map<String, Object>> list(String sort, String dir, int limit) {
        ShipmentSort order = ShipmentSort.parse(sort);
        return search.page(LIST_SELECT, "", QueryFragments.orderBy(order.column(), dir), limit);
    }

    public ShipmentDetail detail(long id) {
        Shipment shipment = shipments.find(id);
        List<Map<String, Object>> peers = shipments.peersByLastStatus(id, String.valueOf(shipment.lastStatus()));
        return new ShipmentDetail(shipment, peers, shipments.events(id));
    }

    public CarrierLoad carrierLoad(long id) {
        Shipment shipment = shipments.find(id);
        return new CarrierLoad(shipment.carrier(), shipments.carrierLoad(shipment.carrier()));
    }

    public LabelRequested requestLabel(long id) {
        Shipment shipment = shipments.find(id);
        String name = LabelNames.forShipment(shipment.id());
        shipments.updateLabel(id, name);
        return new LabelRequested(id, name);
    }

    public LabelFile label(long id) throws IOException {
        Shipment shipment = shipments.find(id);
        if (shipment.labelName() == null) {
            throw new NotFoundException("label for shipment", id);
        }
        return new LabelFile(shipment.labelName(), store.read(FileStore.LABELS, shipment.labelName()));
    }

    public TrackingRefresh refresh(long id) {
        Shipment shipment = shipments.find(id);
        Carrier carrier = Carrier.valueOf(shipment.carrier());
        String base = properties.carrierBaseUrls().get(carrier.key());
        String url = base + UriUtils.encodePathSegment(shipment.trackingNo(), StandardCharsets.UTF_8);
        String body = outbound.fetchText(url);
        return new TrackingRefresh(id, carrier.name(), body == null ? 0 : body.length());
    }

    public CallbackApplied applyCallback(Carrier carrier, CarrierEvent event) {
        Shipment shipment = shipments.findByTracking(carrier.name(), event.trackingNo())
                .orElseThrow(() -> new NotFoundException("shipment", event.trackingNo()));
        shipments.insertEvent(shipment.id(), event.status(), event.detail());
        shipments.updateLastStatus(shipment.id(), event.status());
        ShipmentStatus mapped = ShipmentStatus.fromCarrierText(event.status());
        shipments.updateStatus(shipment.id(), mapped);
        events.publishEvent(new ShipmentStatusChanged(shipment.id(), mapped));
        notifier.statusChanged(shipment, mapped);
        return new CallbackApplied(shipment.id(), mapped.name());
    }

    public NotifyUrlSaved saveNotifyUrl(long id, String url) {
        shipments.find(id);
        shipments.updateNotifyUrl(id, url.trim());
        return new NotifyUrlSaved(id, true);
    }
}
