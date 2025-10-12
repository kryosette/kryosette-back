//package com.example.demo.sevices;
//
//import com.example.demo.security.opaque_tokens.TokenService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class RedisService {
//
//    private final StringRedisTemplate redisTemplate;
//    private final TokenService tokenService;
//
//    /**
//     * Сохраняем маппинг IP -> device hash
//     */
//    public void saveIpToDeviceMapping(String clientIp, String deviceHash) {
//        try {
//            // IP -> device hash (TTL 24 часа)
//            redisTemplate.opsForValue().set(
//                    "ip_device:" + clientIp,
//                    deviceHash,
//                    Duration.ofHours(24)
//            );
//
//            // Device -> IP для обратного поиска
//            redisTemplate.opsForValue().set(
//                    "device_ip:" + deviceHash,
//                    clientIp,
//                    Duration.ofHours(24)
//            );
//
//            log.debug("Saved IP->Device mapping: {} -> {}", clientIp, deviceHash);
//        } catch (Exception e) {
//            log.error("Failed to save IP->Device mapping", e);
//        }
//    }
//
//    /**
//     * Ищем device hash по IP
//     */
//    public String findDeviceHashByIp(String ip) {
//        try {
//            return redisTemplate.opsForValue().get("ip_device:" + ip);
//        } catch (Exception e) {
//            log.error("Failed to find device hash by IP: {}", ip, e);
//            return null;
//        }
//    }
//
//    /**
//     * Ищем username по device hash через токены
//     */
//    public String findUsernameByDeviceHash(String deviceHash) {
//        try {
//            // Ищем все токены с этим device hash
//            // Это упрощенная реализация - в production нужно использовать Redis SCAN
//            // или хранить отдельный индекс device_hash -> username
//
//            // Альтернативно: храним device_hash -> username отдельно
//            String username = redisTemplate.opsForValue().get("device_user:" + deviceHash);
//            if (username != null) {
//                return username;
//            }
//
//            log.warn("No username found for device hash: {}", deviceHash);
//            return null;
//
//        } catch (Exception e) {
//            log.error("Failed to find username by device hash: {}", deviceHash, e);
//            return null;
//        }
//    }
//
//    /**
//     * Сохраняем маппинг device hash -> username
//     */
//    public void saveDeviceToUserMapping(String deviceHash, String username) {
//        try {
//            redisTemplate.opsForValue().set(
//                    "device_user:" + deviceHash,
//                    username,
//                    Duration.ofHours(24)
//            );
//            log.debug("Saved Device->User mapping: {} -> {}", deviceHash, username);
//        } catch (Exception e) {
//            log.error("Failed to save Device->User mapping", e);
//        }
//    }
//
//    /**
//     * Ищем IP по device hash
//     */
//    public String findIpByDeviceHash(String deviceHash) {
//        try {
//            return redisTemplate.opsForValue().get("device_ip:" + deviceHash);
//        } catch (Exception e) {
//            log.error("Failed to find IP by device hash: {}", deviceHash, e);
//            return null;
//        }
//    }
//}
