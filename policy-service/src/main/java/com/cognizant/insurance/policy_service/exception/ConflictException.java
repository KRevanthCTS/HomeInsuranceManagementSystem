package com.cognizant.insurance.policy_service.exception;

// The request is well-formed but clashes with the current state of things - e.g.
// paying a premium that has already been settled in full.
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
