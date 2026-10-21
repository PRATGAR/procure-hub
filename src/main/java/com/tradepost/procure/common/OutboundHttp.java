package com.tradepost.procure.common;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OutboundHttp {

    private final RestTemplate restTemplate;

    public OutboundHttp(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchText(String url) {
        return restTemplate.getForObject(url, String.class);
    }

    public byte[] download(String url) {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        return response.getBody();
    }

    public String deliver(String url, Object payload) {
        return restTemplate.postForObject(url, payload, String.class);
    }
}
