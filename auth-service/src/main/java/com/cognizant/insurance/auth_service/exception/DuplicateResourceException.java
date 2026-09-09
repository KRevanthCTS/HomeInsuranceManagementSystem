package com.cognizant.insurance.auth_service.exception;

// The caller tried to create something that already exists - e.g. registering an
// email that is already taken. Maps to 409 Conflict, not 500.
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
