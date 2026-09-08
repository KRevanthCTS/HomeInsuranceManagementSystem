package com.cognizant.insurance.notification_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cognizant.insurance.notification_service.dto.NotificationRequest;
import com.cognizant.insurance.notification_service.entity.Notification;
import com.cognizant.insurance.notification_service.repository.NotificationRepository;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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

    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    public List<Notification> getForRecipient(String recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }
}
