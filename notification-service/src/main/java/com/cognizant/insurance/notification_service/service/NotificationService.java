package com.cognizant.insurance.notification_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cognizant.insurance.notification_service.dto.NotificationRequest;
import com.cognizant.insurance.notification_service.entity.Notification;
import com.cognizant.insurance.notification_service.repository.NotificationRepository;
import com.cognizant.insurance.notification_service.security.CallerContext;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final CallerContext callerContext;

    public NotificationService(NotificationRepository notificationRepository,
            CallerContext callerContext) {
        this.notificationRepository = notificationRepository;
        this.callerContext = callerContext;
    }

    public Notification send(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setRecipient(request.getRecipient());
        notification.setSubject(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setChannel("EMAIL");
        notification.setStatus("SENT");

        Notification saved = notificationRepository.save(notification);

        // In a real system we'd hand this to an email/SMS provider here. For the
        // project we just log it and keep a record in the DB.
        log.info("Notification -> {} | {} | {}",
                saved.getRecipient(), saved.getSubject(), saved.getMessage());

        return saved;
    }

    // Everyone's mail - an admin-only view.
    public List<Notification> getAll() {
        callerContext.current().requireAdmin("list all notifications");
        return notificationRepository.findAll();
    }

    public List<Notification> getForRecipient(String recipient) {
        callerContext.current().requireOwnerEmail(recipient,
                "read the notifications addressed to " + recipient);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }
}
