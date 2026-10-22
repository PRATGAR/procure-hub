package com.tradepost.procure.config;

import com.tradepost.procure.admin.web.CutoffConverter;
import com.tradepost.procure.common.NumericIdConverter;
import com.tradepost.procure.order.web.ReturnTargetConverter;
import com.tradepost.procure.rfq.web.InvitationTokenConverter;
import com.tradepost.procure.settlement.web.TemplateNameConverter;
import com.tradepost.procure.settlement.web.WatermarkLabelConverter;
import com.tradepost.procure.supplier.web.SearchTermConverter;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TradepostProperties properties;

    public WebConfig(TradepostProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new InvitationTokenConverter());
        registry.addConverter(new SearchTermConverter());
        registry.addConverter(new ReturnTargetConverter(properties.trustedReturnHosts()));
        registry.addConverter(new WatermarkLabelConverter());
        registry.addConverter(new CutoffConverter());
        registry.addConverter(new NumericIdConverter());
        registry.addConverter(new TemplateNameConverter());
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
    }
}
