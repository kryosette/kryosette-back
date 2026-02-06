package com.posts.post.domain.services.posts;

import com.posts.post.domain.aspect.GetToken;
import com.posts.post.domain.services.KryocacheLikeClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeServiceImpl {

    private final KryocacheLikeClient kryocacheLikeClient;
    private final TokenService tokenService;
    private final GetToken getToken;

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

    public Map<String, Object> toggleLike(String token, Long postId) {
        long startTime = System.currentTimeMillis();
        totalLikesProcessed.incrementAndGet();

        try {
            String userId = getToken.verifyTokenAndGetUserId(token);

            if (userId == null) {
                return Map.of(
                        "error", "Invalid token",
                        "liked", false,
                        "timestamp", Instant.now().toString()
                );
            }

            if (!checkRateLimit(userId)) {
                return Map.of(
                        "error", "Too many requests",
                        "retry_after_ms", LIKE_RATE_LIMIT_MS,
                        "timestamp", Instant.now().toString()
                );
            }

            boolean liked = kryocacheLikeClient.toggleUserLike(postId, userId);

            long newCount = 0;
            if (liked) {
                newCount = kryocacheLikeClient.incrementLikeCount(postId);
            } else {
                newCount = kryocacheLikeClient.decrementLikeCount(postId);
            }

            CompletableFuture.runAsync(() -> {
                persistLikeToDatabase(userId, postId, liked);
            });


        } catch () {

        }
    }

    public boolean checkRateLimit(String userId) {
        long now = System.currentTimeMillis();
        AtomicLong lastLikeCheck = userLikeTimestamps.computeIfAbsent(userId, k -> new AtomicLong(0));

        long lastTime = lastLikeCheck.get();
        if (now - lastTime < LIKE_RATE_LIMIT_MS) {
            return false;
        }

        while (true) {
            long current = lastLikeCheck.get();
            if (now - current < LIKE_RATE_LIMIT_MS) {
                return false;
            }
            if (lastLikeTime.compareAndSet(current, now)) {
                return true;
            }
        }
    }

    // === PRIVATE METHODS ===

    @Transactional
    protected void persistLikeToDatabase(String userId, Long postId, boolean liked) {
        // for
        try {
            if (liked) {

            } else {

            }
        } catch (Exception e) {
            log.error("Failed to persist like to database: user={}, post={}, error={}",
                    userId, postId, e.getMessage(), e);

            retryPersistLike(userId, postId, liked);
        }
    }

    // save to kryocache (!)
    private void retryPersistLike(String userId, Long postId, boolean liked) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            //
        }
    }

    // === STATS ===
    public Map<String, Object> getServiceStats() {
        KryocacheLikeClient.LikeStats clientStats = likeClient.getStats();

        return Map.of(
                "total_likes_processed", totalLikesProcessed.get(),
                "successful_likes", successfulLikes.get(),
                "success_rate", totalLikesProcessed.get() > 0 ?
                        (successfulLikes.get() * 100.0 / totalLikesProcessed.get()) : 0,
                "kryocache_stats", Map.of(
                        "total_operations", clientStats.totalOperations(),
                        "failed_operations", clientStats.failedOperations(),
                        "current_load", clientStats.currentLoad(),
                        "cached_counters", clientStats.cachedCounters(),
                        "cached_statuses", clientStats.cachedStatuses()
                ),
                "active_users", userLikeTimestamps.size(),
                "timestamp", Instant.now().toString()
        );
    }
}
