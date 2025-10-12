package com.example.demo.sevices;

import lombok.Data;

@Data
public class BanRequest {
    private String deviceHash;
    private String reason;
    private int duration;
    private String level;
}