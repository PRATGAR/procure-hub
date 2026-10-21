package com.tradepost.procure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/portal/**", "/partner/**", "/error").permitAll()
                        .requestMatchers("/api/shipments/callbacks/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/integration/inbound/*").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/orders/*/approve").hasAnyRole("APPROVER", "ADMIN")
                        .requestMatchers("/api/settlements/**").hasAnyRole("FINANCE", "ADMIN")
                        .requestMatchers("/api/pricing/**", "/api/reports/**").hasAnyRole("BUYER", "FINANCE", "ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("buyer").password(encoder.encode("buyer-pass")).roles("BUYER").build(),
                User.withUsername("approver").password(encoder.encode("approver-pass")).roles("BUYER", "APPROVER").build(),
                User.withUsername("finance").password(encoder.encode("finance-pass")).roles("FINANCE").build(),
                User.withUsername("ops").password(encoder.encode("ops-pass")).roles("ADMIN", "BUYER", "FINANCE", "APPROVER").build());
    }
}
