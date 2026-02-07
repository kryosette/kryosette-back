package com.posts.common.auth.aspect;

import lombok.Data;

@Data
public class AuthVerifyResponse {
    private String userId;
    private String email;
    private String username;
}