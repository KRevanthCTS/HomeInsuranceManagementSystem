package com.cognizant.insurance.claim_service.exception;

// The caller is authenticated but not allowed to do this - e.g. a CUSTOMER
// trying to approve a claim, or read someone else's.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
