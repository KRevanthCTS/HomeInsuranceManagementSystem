package com.cognizant.insurance.notification_service.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

// Resolves the Caller for whichever request the current thread is serving, so
// services can do their own authorization without every controller method having
// to pass three header parameters down.
@Component
public class CallerContext {

    public Caller current() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();

        return new Caller(
                request.getHeader("X-User-Email"),
                request.getHeader("X-User-Role"),
                parseUserId(request.getHeader("X-User-Id")));
    }

    private Long parseUserId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

