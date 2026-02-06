package com.example.demo.domain.responses.auth.verify.token;

import lombok.Data;

@Data
public class AuthVerifyResponse {
    private String userId;
    private String email;
    private String username;
}