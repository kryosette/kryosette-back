package com.example.demo.user.profile.status;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_status")
@Getter
@Setter
@NoArgsConstructor
public class UserStatus {
    @Id
    private String userEmail;

    private boolean online;
    private LocalDateTime lastSeen;

    public UserStatus(String userEmail) {
        this.userEmail = userEmail;
        this.online = false;
        this.lastSeen = LocalDateTime.now();
    }
}