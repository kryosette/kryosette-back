package com.example.demo.security.opaque_tokens;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenDataDev {
    private String userId;
    private long creationTime;
    private String userData;
}
