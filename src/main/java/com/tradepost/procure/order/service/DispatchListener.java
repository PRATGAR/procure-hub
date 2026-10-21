package com.tradepost.procure.order.service;

import com.tradepost.procure.supplier.Supplier;
import com.tradepost.procure.supplier.service.SupplierService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class DispatchListener {

    private static final Logger log = LoggerFactory.getLogger(DispatchListener.class);

    private final Map<String, DispatchChannel> channels;
    private final SupplierService suppliers;

    public DispatchListener(Map<String, DispatchChannel> channels, SupplierService suppliers) {
        this.channels = channels;
        this.suppliers = suppliers;
    }

    @EventListener
    public void onOrderApproved(OrderApproved event) {
        Supplier supplier = suppliers.get(event.supplierId());
        String channelName = "ACTIVE".equals(supplier.status()) ? "webhook" : "portal";
        DispatchChannel channel = channels.get(channelName);
        try {
            channel.dispatch(event);
        } catch (RestClientException e) {
            log.warn("dispatch of order {} through {} failed", event.orderId(), channelName);
        }
    }
}
