package com.cognizant.insurance.api_gateway.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    // Proves to the downstream services that a request actually came through the
    // gateway. Without it they would have to trust X-User-* from anyone who can
    // reach their port directly.
    public static final String GATEWAY_AUTH_HEADER = "X-Gateway-Auth";

    // Paths that anyone can hit without a token. Matched exactly - a prefix match
    // here would make e.g. /auth/registerFOO public too.
    private static final List<String> OPEN_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/health"
    );

    private final JwtUtil jwtUtil;
    private final String gatewaySecret;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
            @Value("${gateway.shared-secret}") String gatewaySecret) {
        this.jwtUtil = jwtUtil;
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isOpen(path)) {
            // Still stamp the gateway secret so the downstream service knows the
            // call came through us, but there is no identity to attach yet.
            return chain.filter(exchange.mutate().request(
                    exchange.getRequest().mutate()
                            .header(GATEWAY_AUTH_HEADER, gatewaySecret)
                            .headers(h -> h.remove("X-User-Email"))
                            .headers(h -> h.remove("X-User-Role"))
                            .headers(h -> h.remove("X-User-Id"))
                            .build())
                    .build());
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
        Long userId = jwtUtil.extractUserId(token);

        // Hand the identity to the downstream service via trusted headers. These
        // are set (not appended), so anything the client sent under these names is
        // discarded rather than passed through.
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .header("X-User-Id", userId == null ? "" : String.valueOf(userId))
                .header(GATEWAY_AUTH_HEADER, gatewaySecret)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isOpen(String path) {
        return OPEN_PATHS.contains(path);
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
