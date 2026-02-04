package com.example.demo.domain.services.opaque_tokens;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

public class KryocacheLikeClient {
    @Value("${kryocache.server.host:0:0:0:0:0:0:0:1}")
    private String host;

    @Value("${kryocache.server.port:6898}")
    private int port;

    @Value("${kryocache.server.timeout:100}") // Уменьшаем timeout для лайков
    private int timeout;

    @Value("${kryocache.server.max-retries:2}")
    private int maxRetries;

    private final ReentrantLock lock = new ReentrantLock();

    /*
┌─────────────────────────────────────────────────────┐
│                   ЯДРО 0                            │
│  ┌──────────────────────────────────────────────┐   │
│  │  Регистры (1 цикл)                           │   │
│  │  ┌──────────────────────────────────────┐    │   │
│  │  │   L1 Кэш (1-3 цикла)                 │    │   │
│  │  │  ┌──────────┐  ┌──────────┐          │    │   │
│  │  │  │ L1d Data │  │ L1i Inst │          │    │   │
│  │  │  │  32-64KB │  │  32-64KB │          │    │   │
│  │  │  └──────────┘  └──────────┘          │    │   │
│  │  └──────────────────────────────────────┘    │   │
│  │                 ↓ (12-20 циклов)             │   │
│  │  ┌──────────────────────────────────────┐    │   │
│  │  │   L2 Кэш (256KB - 1MB)               │    │   │
│  │  │  Unified (данные + инструкции)       │    │   │
│  │  └──────────────────────────────────────┘    │   │
│  └──────────────────────────────────────────────┘   │
│                     ↓ (30-60 циклов)                │
│  ┌────────────────────────────────────────────────┐ │
│  │           L3 Кэш (Shared, 8-128MB)             │ │
│  │  Общий для всех ядер, используется как         │ │
│  │  когерентный буфер (LLC - Last Level Cache)    │ │
│  └────────────────────────────────────────────────┘ │
│                     ↓ (200-400 циклов)              │
│                   ОЗУ (RAM)                         │
└─────────────────────────────────────────────────────┘
     */
}
