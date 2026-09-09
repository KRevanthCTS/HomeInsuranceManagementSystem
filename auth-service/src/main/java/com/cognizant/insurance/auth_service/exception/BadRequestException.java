package com.cognizant.insurance.auth_service.exception;

// For things the caller got wrong, e.g. a role that isn't one of ours.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
