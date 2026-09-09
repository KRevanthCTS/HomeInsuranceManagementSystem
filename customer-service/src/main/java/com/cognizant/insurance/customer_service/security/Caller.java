package com.cognizant.insurance.customer_service.security;

import com.cognizant.insurance.customer_service.exception.ForbiddenException;

// Who is making the current request, as told to us by the gateway. The gateway
// validated the JWT and set these headers itself, discarding anything the client
// sent under the same names (see api-gateway JwtAuthenticationFilter).
public class Caller {

    private static final String ADMIN = "ADMIN";

    private final String email;
    private final String role;
    private final Long userId;

    public Caller(String email, String role, Long userId) {
        this.email = email;
        this.role = role;
        this.userId = userId;
    }

    public String email() {
        return email;
    }

    public String role() {
        return role;
    }

    public Long userId() {
        return userId;
    }

    public boolean isAdmin() {
        return ADMIN.equalsIgnoreCase(role);
    }

    public void requireAdmin(String action) {
        if (!isAdmin()) {
            throw new ForbiddenException("Only an ADMIN may " + action);
        }
    }

    // Admins may act on any row; everyone else only on rows tied to their own
    // auth user id.
    public void requireOwner(Long ownerUserId, String action) {
        if (isAdmin()) {
            return;
        }
        if (userId == null || ownerUserId == null || !userId.equals(ownerUserId)) {
            throw new ForbiddenException("You may not " + action);
        }
    }
}
