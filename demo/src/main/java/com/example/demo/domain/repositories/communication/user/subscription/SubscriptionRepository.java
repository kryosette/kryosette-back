package com.example.demo.domain.repositories.communication.user.subscription;

import com.example.demo.domain.model.user.subscription.Subscription;
import jakarta.persistence.LockModeType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s " +
            "WHERE s.followerEmail = :followerEmail " +
            "AND s.followingEmail = :followingEmail")
    Optional<Subscription> findSubscriptionWithLock(
            @Param("followerEmail") String followerEmail,
            @Param("followingEmail") String followingEmail);

    @Cacheable(value = "subscriptions", key = "{#followerEmail, #followingEmail}")
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Subscription s " +
            "WHERE s.followerEmail = :followerEmail AND s.followingEmail = :followingEmail")
    boolean existsSubscription(@Param("followerEmail") String followerEmail,
                               @Param("followingEmail") String followingEmail);

    @Query("SELECT s FROM Subscription s " +
            "WHERE s.followerEmail = :followerEmail AND s.followingEmail = :followingEmail")
    Optional<Subscription> findSubscription(@Param("followerEmail") String followerEmail,
                                            @Param("followingEmail") String followingEmail);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.followingEmail = :email")
    long countFollowers(@Param("email") String email);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.followerEmail = :email")
    long countSubscriptions(@Param("email") String email);
}