package com.substring.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@RequiredArgsConstructor
public class ClearCache {
    private static final String USER_ROOMS_KEY_PREFIX = "user_rooms:";
    private static final String PRIVATE_ROOM_KEY_PREFIX = "private_room:";
    private final StringRedisTemplate redisTemplate;

    private void clearUserRoomsCache(String userId) {
        try {
            redisTemplate.delete(USER_ROOMS_KEY_PREFIX + userId);
            log.debug("Cleared rooms cache for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to clear cache for user {}", userId, e);
        }
    }

    private void clearPrivateRoomCache(Long roomId) {
        try {
            redisTemplate.delete(PRIVATE_ROOM_KEY_PREFIX + roomId);
            log.debug("Cleared cache for room: {}", roomId);
        } catch (Exception e) {
            log.error("Failed to clear cache for room {}", roomId, e);
        }
    }
}
