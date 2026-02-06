package com.posts.post.domain.services.posts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeServiceImpl {

    private final KryocacheLikeClient kryocacheLikeClient;
    private final TokenService tokenService;
    private final

    // rate limiting per user
    private final Map<String, AtomicLong> userLikeTimestamps = new ConcurrentHashMap<>();
    /*
    In Java, time is typically measured in milliseconds (as in System.currentTimeMillis()),
    and a 64-bit long type is used to store such values long to avoid overflow during calculations.
     */
    private static final long LIKE_RATE_LIMIT_MS = 100;

    // Statistics
    private final AtomicLong totalLikesProcessed = new AtomicLong(0);
    private final AtomicLong successfulLikes = new AtomicLong(0);

    public Map<String, Object> toggleLike(String token, String postId) {
        long startTime = System.currentTimeMillis();
        totalLikesProcessed.incrementAndGet();

        try {
            String tokenData = tokenService.validateToken(token);
        } catch () {

        }
    }
}
