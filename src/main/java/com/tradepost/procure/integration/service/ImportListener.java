package com.tradepost.procure.integration.service;

import com.tradepost.procure.common.InvalidRequestException;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ImportListener {

    private final Map<String, PayloadDecoder> decoders;

    public ImportListener(Map<String, PayloadDecoder> decoders) {
        this.decoders = decoders;
    }

    @EventListener
    public void onImportSubmitted(ImportSubmitted event) {
        PayloadDecoder decoder = decoders.get(event.format());
        if (decoder == null) {
            throw new InvalidRequestException("unsupported import format");
        }
        try {
            event.result().set(decoder.decode(event.payload()));
        } catch (Exception e) {
            throw new InvalidRequestException("payload could not be decoded");
        }
    }
}
