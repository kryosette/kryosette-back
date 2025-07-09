package com.example.demo.user.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Получение непрочитанных уведомлений для пользователя
    List<Notification> findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(String email);

    // Получение всех уведомлений с пагинацией
    @Query("SELECT n FROM Notification n WHERE n.recipientEmail = :email ORDER BY n.createdAt DESC")
    List<Notification> findUserNotifications(String email, Pageable pageable);

    // Пометить уведомления как прочитанные
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :ids AND n.recipientEmail = :email")
    void markAsRead(List<Long> ids, String email);

    // Количество непрочитанных уведомлений
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientEmail = :email AND n.isRead = false")
    long countUnreadNotifications(String email);

    // Удаление старых уведомлений (более 30 дней)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipientEmail = :email AND n.createdAt < :cutoffDate")
    void deleteOldNotifications(LocalDateTime cutoffDate);

    Page<Notification> findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(
            String email,
            Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE n.recipientEmail = :email ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientEmail(@Param("email") String email, Pageable pageable);
}