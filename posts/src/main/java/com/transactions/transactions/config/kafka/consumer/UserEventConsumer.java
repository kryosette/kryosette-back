//package com.transactions.transactions.config.kafka.consumer;
//
//import com.transactions.transactions.config.kafka.events.UserEvent;
//import com.transactions.transactions.config.kafka.events.dto.CachedUser;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//public class UserEventConsumer {
//    private final Map<String, CachedUser> userCache = new ConcurrentHashMap<>();
//
//    @KafkaListener(topics = "user-events")
//    public void handle(UserEvent event) {
//        userCache.put(event.getId(),
//                new CachedUser(event.getId(), event.getUsername()));
//    }
//
//    public CachedUser getCachedUser(String authorId) {
//        return userCache.get(authorId);
//    }
//}