package com.innowise.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

import static com.innowise.apigateway.config.AuthConstant.BEARER_PREFIX_LENGTH;
import static com.innowise.apigateway.config.AuthConstant.TOKEN_PREFIX;

/**
 * @ClassName SecurityConfig
 * @Description Security configuration for API Gateway.
 * Configures JWT authentication filter and route-based authorization.
 * @Author dshparko
 * @Date 28.10.2025 22:07
 * @Version 1.0
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         JwtFilter jwtAuthManager) {
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthManager);
        jwtFilter.setServerAuthenticationConverter(exchange -> extractToken(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)));

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/auth/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private Mono<Authentication> extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX_LENGTH);
            return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
        }
        return Mono.empty();
    }

}
