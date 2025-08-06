package com.substring.chat.applications.dtos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(String userId);
    int countByRecipientIdAndIsReadFalse(String userId);
}
