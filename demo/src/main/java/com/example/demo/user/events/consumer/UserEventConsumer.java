//package com.example.demo.user.events.consumer;
//
//import com.example.demo.user.events.UserEvent;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//public class UserEventConsumer {
//    private final Map<Long, CachedUser> userCache = new ConcurrentHashMap<>();
//
//    @KafkaListener(topics = "user-events")
//    public void handle(UserEvent event) {
//        userCache.put(event.getId(),
//                new CachedUser(event.getId(), event.getUsername()));
//    }
//
//    public CachedUser getCachedUser(Long userId) {
//        return userCache.get(userId);
//    }
//}
