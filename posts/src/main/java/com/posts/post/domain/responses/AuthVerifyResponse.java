package com.posts.post.domain.responses;

import lombok.Data;

@Data
public class AuthVerifyResponse {
    private String userId;
    private String email;
    private String username;
}