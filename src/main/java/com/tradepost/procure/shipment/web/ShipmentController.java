package com.tradepost.procure.shipment.web;

import com.tradepost.procure.shipment.Carrier;
import com.tradepost.procure.shipment.service.ShipmentService;
import com.tradepost.procure.shipment.web.ShipmentDtos.CallbackApplied;
import com.tradepost.procure.shipment.web.ShipmentDtos.CarrierEvent;
import com.tradepost.procure.shipment.web.ShipmentDtos.CarrierLoad;
import com.tradepost.procure.shipment.web.ShipmentDtos.Created;
import com.tradepost.procure.shipment.web.ShipmentDtos.CreateShipmentRequest;
import com.tradepost.procure.shipment.web.ShipmentDtos.LabelFile;
import com.tradepost.procure.shipment.web.ShipmentDtos.LabelRequested;
import com.tradepost.procure.shipment.web.ShipmentDtos.NotifyRequest;
import com.tradepost.procure.shipment.web.ShipmentDtos.NotifyUrlSaved;
import com.tradepost.procure.shipment.web.ShipmentDtos.ShipmentDetail;
import com.tradepost.procure.shipment.web.ShipmentDtos.TrackingRefresh;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
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
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService service;

    public ShipmentController(ShipmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Created> create(@Valid @RequestBody CreateShipmentRequest request) {
        long id = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Created(id, "CREATED"));
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String sort,
                                          @RequestParam(required = false) String dir,
                                          @RequestParam(defaultValue = "25") int limit) {
        return service.list(sort, dir, limit);
    }

    @GetMapping("/{id}")
    public ShipmentDetail detail(@PathVariable long id) {
        return service.detail(id);
    }

    @GetMapping("/{id}/carrier-load")
    public CarrierLoad carrierLoad(@PathVariable long id) {
        return service.carrierLoad(id);
    }

    @PostMapping("/{id}/label")
    public LabelRequested requestLabel(@PathVariable long id) {
        return service.requestLabel(id);
    }

    @GetMapping("/{id}/label")
    public byte[] label(@PathVariable long id, HttpServletResponse response) throws IOException {
        LabelFile file = service.label(id);
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.name() + "\"");
        return file.content();
    }

    @PostMapping("/{id}/refresh")
    public TrackingRefresh refresh(@PathVariable long id) {
        return service.refresh(id);
    }

    @PutMapping("/{id}/notify-url")
    public NotifyUrlSaved saveNotifyUrl(@PathVariable long id, @Valid @RequestBody NotifyRequest request) {
        return service.saveNotifyUrl(id, request.url());
    }

    @PostMapping("/callbacks/{carrier}")
    public CallbackApplied callback(@PathVariable Carrier carrier, @Valid @RequestBody CarrierEvent event) {
        return service.applyCallback(carrier, event);
    }
}
