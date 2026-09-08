package com.cognizant.insurance.claim_service.exception;

// For things the caller got wrong, e.g. an unknown policy number or bad status value.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
