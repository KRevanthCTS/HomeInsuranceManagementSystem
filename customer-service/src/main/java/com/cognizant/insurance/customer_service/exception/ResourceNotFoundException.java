package com.cognizant.insurance.customer_service.exception;

// Thrown when a customer we were asked for simply isn't there.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
