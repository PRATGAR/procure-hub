package com.tradepost.procure.document.service;

import com.tradepost.procure.common.InvalidRequestException;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PublishListener {

    private final Map<String, DocumentConverter> converters;

    public PublishListener(Map<String, DocumentConverter> converters) {
        this.converters = converters;
    }

    @EventListener
    public void onDocumentPublished(DocumentPublished event) {
        DocumentConverter converter = converters.get(event.format());
        if (converter == null) {
            throw new InvalidRequestException("unsupported publish format");
        }
        event.result().set(converter.convert(event.document(), event.prefix()));
    }
}
