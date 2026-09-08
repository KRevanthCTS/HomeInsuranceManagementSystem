package com.cognizant.insurance.api_gateway.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

// Central authentication. Instead of every microservice re-checking the JWT,
// the gateway does it once here. Public endpoints (login/register) are skipped.
// For everything else we require a valid Bearer token and, once it checks out,
// we stamp the caller's email + role onto the request as headers so the
// downstream service can trust them without parsing the token again.
@Component
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Paths that anyone can hit without a token.
    private static final List<String> OPEN_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/health"
    );

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isOpen(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No bearer token on protected path {}", path);
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.debug("Rejected invalid/expired token for {}", path);
            return unauthorized(exchange);
        }

        String email = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        // Hand the identity to the downstream service via trusted headers.
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isOpen(String path) {
        return OPEN_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Run early, before the routing happens.
        return -1;
    }
}
