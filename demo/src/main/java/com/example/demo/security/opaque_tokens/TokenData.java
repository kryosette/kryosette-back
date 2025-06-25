package com.example.demo.security.opaque_tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenData {
    private String tokenId;
    private String userId;
    private String username;
    private List<String> authorities;
    private String deviceHash;
    private Instant issuedAt;
    private Instant expiresAt;
}