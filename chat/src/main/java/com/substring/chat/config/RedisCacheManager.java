//package com.substring.chat.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//
//@Bean
//public org.springframework.data.redis.cache.RedisCacheManager cacheManager(RedisConnectionFactory factory) {
//    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
//            .entryTtl(Duration.ofSeconds(30));  // Дефолтный TTL = 30 сек
//
//    Map<String, RedisCacheConfiguration> configs = new HashMap<>();
//    configs.put("privateMessage", RedisCacheConfiguration.defaultCacheConfig()
//            .entryTtl(Duration.ofMinutes(10)));  // Для "products" TTL = 10 минут
//
//    return org.springframework.data.redis.cache.RedisCacheManager.builder(factory)
//            .cacheDefaults(defaultConfig)
//            .withInitialCacheConfigurations(configs)
//            .build();
//}