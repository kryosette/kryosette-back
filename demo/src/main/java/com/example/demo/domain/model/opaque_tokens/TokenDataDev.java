package com.example.demo.domain.model.opaque_tokens;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TokenDataDev {
    private String tokenId;
    private String userId;
    private String username;
    private List<String> authorities;
    private String deviceHash;
    private String clientIp;
    private long issuedAt;
    private long expiresAt;
    private String userData;
}