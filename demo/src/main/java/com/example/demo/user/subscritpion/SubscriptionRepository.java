package com.example.demo.user.subscritpion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

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