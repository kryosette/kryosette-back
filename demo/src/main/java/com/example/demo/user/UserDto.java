package com.example.demo.user;

import lombok.Data;

@Data
public class UserDto {
    private String firstname;
    private String lastname;
    private String email;
    private String userId;
    private String username;

    public UserDto(String username, String email) {
        this.username = username;
        this.email = email;
    }
}