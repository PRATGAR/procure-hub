package com.tradepost.procure.integration.service;

import java.util.Map;

public interface PayloadDecoder {

    Map<String, Object> decode(byte[] payload) throws Exception;
}
