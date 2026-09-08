package com.cognizant.insurance.notification_service.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationRequest {

    @NotBlank(message = "recipient is required")
    private String recipient;

    private String subject;

    @NotBlank(message = "message is required")
    private String message;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
