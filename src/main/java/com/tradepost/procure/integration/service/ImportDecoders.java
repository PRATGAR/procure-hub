package com.tradepost.procure.integration.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImportDecoders {

    @Bean
    public JavaSerialDecoder javaDecoder() {
        return new JavaSerialDecoder(SerialFilters.importFormats());
    }
}
