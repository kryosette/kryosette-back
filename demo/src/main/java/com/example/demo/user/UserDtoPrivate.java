package com.example.demo.user;

import lombok.Data;

@Data
public class UserDtoPrivate {
    private String firstname;
    private String lastname;
    private String email;
    private String userId;
    private String username;

    public UserDtoPrivate(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}