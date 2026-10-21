package com.tradepost.procure.config;

import com.tradepost.procure.admin.web.CutoffConverter;
import com.tradepost.procure.admin.web.StatColumnConverter;
import com.tradepost.procure.catalog.web.SpecFieldConverter;
import com.tradepost.procure.common.RequestContextInterceptor;
import com.tradepost.procure.directory.web.UidConverter;
import com.tradepost.procure.document.web.ArchiveNameConverter;
import com.tradepost.procure.order.web.ReturnTargetConverter;
import com.tradepost.procure.rfq.web.InvitationTokenConverter;
import com.tradepost.procure.rfq.web.PortalHeaderFilter;
import com.tradepost.procure.settlement.web.WatermarkLabelConverter;
import com.tradepost.procure.shipment.web.ExportNameConverter;
import com.tradepost.procure.supplier.web.SearchTermConverter;
import com.tradepost.procure.supplier.web.TaxIdConverter;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TradepostProperties properties;

    public WebConfig(TradepostProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestContextInterceptor())
                .addPathPatterns("/api/rfqs/**", "/api/directory/**");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new InvitationTokenConverter());
        registry.addConverter(new UidConverter());
        registry.addConverter(new SearchTermConverter());
        registry.addConverter(new TaxIdConverter());
        registry.addConverter(new SpecFieldConverter());
        registry.addConverter(new ReturnTargetConverter(properties.trustedReturnHosts()));
        registry.addConverter(new WatermarkLabelConverter());
        registry.addConverter(new ExportNameConverter());
        registry.addConverter(new ArchiveNameConverter());
        registry.addConverter(new StatColumnConverter());
        registry.addConverter(new CutoffConverter());
    }

    @Bean
    public FilterRegistrationBean<PortalHeaderFilter> portalHeaderFilter() {
        FilterRegistrationBean<PortalHeaderFilter> registration = new FilterRegistrationBean<>(new PortalHeaderFilter());
        registration.addUrlPatterns("/portal/*");
        return registration;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
    }
}
