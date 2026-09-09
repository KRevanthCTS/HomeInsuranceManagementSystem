package com.cognizant.insurance.notification_service.exception;

// The caller is authenticated but not allowed to read these notifications.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
