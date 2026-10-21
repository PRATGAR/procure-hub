package com.tradepost.procure.config;

import com.tradepost.procure.common.RequestContextInterceptor;
import com.tradepost.procure.directory.web.UidConverter;
import com.tradepost.procure.rfq.web.InvitationTokenConverter;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestContextInterceptor())
                .addPathPatterns("/api/rfqs/**", "/api/directory/**");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new InvitationTokenConverter());
        registry.addConverter(new UidConverter());
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
    }
}
