package com.cognizant.insurance.claim_service.client;

// Payload we send to notification-service. Mirrors its NotificationRequest DTO.
public class NotificationRequest {

    private String recipient;
    private String subject;
    private String message;

    public NotificationRequest() {}

    public NotificationRequest(String recipient, String subject, String message) {
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
    }

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
