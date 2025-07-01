package com.example.demo.user.subscritpion;

import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false, length = 255)
    private String followerEmail;

    @Column(name = "following_id", nullable = false, length = 255)
    private String followingEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}