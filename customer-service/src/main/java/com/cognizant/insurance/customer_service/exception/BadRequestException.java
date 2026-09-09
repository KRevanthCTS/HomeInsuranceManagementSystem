package com.cognizant.insurance.customer_service.exception;

// For things the caller got wrong, e.g. a risk factor that isn't one of ours.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
