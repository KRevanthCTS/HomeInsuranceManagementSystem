package com.cognizant.insurance.claim_service.exception;

// The request is well-formed but clashes with the current state of things - e.g.
// filing a claim that has already been filed for the same incident.
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
