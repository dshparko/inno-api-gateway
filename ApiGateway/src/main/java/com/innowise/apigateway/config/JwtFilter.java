package com.innowise.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.apigateway.exception.InvalidResourceException;
import com.innowise.apigateway.model.dto.ErrorResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.innowise.apigateway.config.AuthConstant.CONTENT_TYPE_JSON;
import static com.innowise.apigateway.config.AuthConstant.HEADER_USER_ID;
import static com.innowise.apigateway.config.AuthConstant.TOKEN_PREFIX;


/**
 * Global JWT filter for API Gateway.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtFilter implements GlobalFilter, ReactiveAuthenticationManager {

    private final ObjectMapper objectMapper;
    private final SecretKey key;
    private final List<String> publicPaths = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/actuator"
    );

    public JwtFilter(@Value("${jwt.secret}") String secret, ObjectMapper objectMapper) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (token == null) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        try {
            Claims claims = parseClaims(token);
            String userId = claims.getSubject();

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(HEADER_USER_ID, userId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            return unauthorized(exchange, "Invalid or expired JWT");
        }
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = String.valueOf(authentication.getCredentials());
        try {
            Claims claims = parseClaims(token);
            String userId = claims.getSubject();

            List<String> roles = claims.get("roles", List.class);
            var authorities = roles == null
                    ? List.<SimpleGrantedAuthority>of()
                    : roles.stream().map(SimpleGrantedAuthority::new).toList();

            return Mono.just(new UsernamePasswordAuthenticationToken(userId, token, authorities));
        } catch (Exception e) {
            return Mono.empty();
        }
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private String extractToken(String authHeader) {
        return (authHeader != null && authHeader.startsWith(TOKEN_PREFIX))
                ? authHeader.substring(TOKEN_PREFIX.length())
                : null;
    }

    private Claims parseClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(new Date())) {
            throw new InvalidResourceException("Token expired");
        }
        return claims;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                message,
                exchange.getRequest().getPath().value(),
                UUID.randomUUID()
        );

        return response.writeWith(Mono.fromSupplier(() -> {
            try {
                return response.bufferFactory().wrap(objectMapper.writeValueAsBytes(errorResponse));
            } catch (Exception e) {
                return response.bufferFactory().wrap("{\"error\":\"Serialization failed\"}".getBytes());
            }
        }));
    }

}