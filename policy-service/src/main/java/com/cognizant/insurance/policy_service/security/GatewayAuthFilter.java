package com.cognizant.insurance.policy_service.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// This service authorizes on the X-User-* headers the gateway stamps, so it must
// only accept requests that really came through the gateway. Without this check
// anyone able to reach port 8083 could send "X-User-Role: ADMIN" and read every
// policy in the system.
//
// Sibling services calling in over Feign forward the same secret.
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    public static final String GATEWAY_AUTH_HEADER = "X-Gateway-Auth";

    private static final Logger log = LoggerFactory.getLogger(GatewayAuthFilter.class);

    private final byte[] expectedSecret;

    public GatewayAuthFilter(@Value("${gateway.shared-secret}") String expectedSecret) {
        this.expectedSecret = expectedSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String presented = request.getHeader(GATEWAY_AUTH_HEADER);
        if (presented == null || !MessageDigest.isEqual(
                presented.getBytes(StandardCharsets.UTF_8), expectedSecret)) {
            log.warn("Blocked a request to {} that did not come through the gateway",
                    request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Requests must come through the API gateway");
            return;
        }

        chain.doFilter(request, response);
    }
}
