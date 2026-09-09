package com.cognizant.insurance.policy_service.service;

import org.springframework.stereotype.Service;

import com.cognizant.insurance.policy_service.client.CustomerClient;
import com.cognizant.insurance.policy_service.client.CustomerDto;
import com.cognizant.insurance.policy_service.exception.ForbiddenException;
import com.cognizant.insurance.policy_service.security.Caller;
import com.cognizant.insurance.policy_service.security.CallerContext;

import feign.FeignException;

// Properties, policies and payments all hang off a customerId, but the gateway
// only tells us the caller's auth user id. This turns one into the other by
// asking customer-service, and is the single place ownership is decided.
@Service
public class OwnershipService {

    private final CustomerClient customerClient;
    private final CallerContext callerContext;

    public OwnershipService(CustomerClient customerClient, CallerContext callerContext) {
        this.customerClient = customerClient;
        this.callerContext = callerContext;
    }

    // The customer profile of whoever is calling, or null for an ADMIN - which
    // callers read as "no restriction".
    public Long callerCustomerId() {
        Caller caller = callerContext.current();
        if (caller.isAdmin()) {
            return null;
        }
        if (caller.userId() == null) {
            throw new ForbiddenException("Your token carries no user id; please log in again");
        }
        try {
            CustomerDto customer = customerClient.getByUserId(caller.userId());
            return customer.getCustomerId();
        } catch (FeignException.NotFound | FeignException.Forbidden e) {
            throw new ForbiddenException(
                    "You need a customer profile before using policies. Create one via POST /customers.");
        }
    }

    public void requireOwns(Long customerId, String action) {
        Long mine = callerCustomerId();
        if (mine == null) {
            return;
        }
        if (customerId == null || !mine.equals(customerId)) {
            throw new ForbiddenException("You may not " + action);
        }
    }

    public boolean callerIsAdmin() {
        return callerContext.current().isAdmin();
    }

    public void requireAdmin(String action) {
        callerContext.current().requireAdmin(action);
    }
}
