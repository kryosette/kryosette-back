package com.example.demo.user.events;

import com.example.demo.user.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserEvent {
    private String type;
    private String userId;
    private String username;

    public UserEvent(String type, String userId, String username) {
        this.type = type;
        this.userId = userId;
        this.username = username;
    }
}
