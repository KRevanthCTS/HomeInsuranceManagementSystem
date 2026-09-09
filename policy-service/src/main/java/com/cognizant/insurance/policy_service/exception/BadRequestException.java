package com.cognizant.insurance.policy_service.exception;

// For things the caller got wrong, e.g. a property type that isn't one of ours
// or a payment larger than what is still owed.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
