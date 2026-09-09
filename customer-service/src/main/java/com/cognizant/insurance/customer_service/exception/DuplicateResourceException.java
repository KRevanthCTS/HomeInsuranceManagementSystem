package com.cognizant.insurance.customer_service.exception;

// The caller tried to create something that already exists - e.g. a second
// customer profile for a user who already has one. Maps to 409 Conflict.
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
