package com.cognizant.insurance.customer_service.exception;

// The caller is authenticated but not allowed to touch this particular row.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
