package com.cognizant.insurance.claim_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

// Feign starts a brand new HTTP request, so none of the caller's headers come
// along by default. Without this, customer-service would see an anonymous call
// with no gateway secret and reject it.
//
// We forward the gateway secret (so the callee knows this is internal traffic)
// and the caller's identity (so the callee can apply the same ownership rules we
// would have applied).
@Configuration
public class FeignIdentityPropagation {

    private static final String[] FORWARDED_HEADERS = {
        "X-Gateway-Auth", "X-User-Email", "X-User-Role", "X-User-Id"
    };

    @Bean
    public RequestInterceptor identityPropagatingInterceptor(
            @Value("${gateway.shared-secret}") String gatewaySecret) {

        return template -> {
            HttpServletRequest incoming = currentRequest();
            if (incoming == null) {
                // No request in scope (e.g. a scheduled task). Still identify
                // ourselves as internal traffic.
                template.header("X-Gateway-Auth", gatewaySecret);
                return;
            }
            for (String name : FORWARDED_HEADERS) {
                String value = incoming.getHeader(name);
                if (value != null && !value.isBlank()) {
                    template.header(name, value);
                }
            }
        };
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (attributes instanceof ServletRequestAttributes servletAttributes)
                ? servletAttributes.getRequest()
                : null;
    }
}

