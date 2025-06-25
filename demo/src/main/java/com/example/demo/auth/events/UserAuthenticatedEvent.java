package com.example.demo.auth.events;

import com.example.demo.user.User;

public class UserAuthenticatedEvent {
    private String userId;
    private String token;

    public UserAuthenticatedEvent(User user, String jwtToken, long l) {
        this.userId = user.getId();
        this.token = jwtToken;
    }

    // constructors, getters, setters
}