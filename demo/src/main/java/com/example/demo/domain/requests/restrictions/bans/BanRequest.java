package com.example.demo.domain.requests.restrictions.bans;

import lombok.Data;

@Data
public class BanRequest {
    private String deviceHash;
    private String reason;
    private int duration;
    private String level;
}