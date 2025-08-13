package com.posts.post.infrastructure.metrics;

public record Stats(
        int count,
        double mean,
        double stdDev,
        long median,
        long p90,
        long p95,
        long p99,
        long min,
        long max
) {}
