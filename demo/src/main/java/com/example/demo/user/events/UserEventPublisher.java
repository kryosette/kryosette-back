package com.example.demo.user.events;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {
    private static final String TOPIC = "user-events";

    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void publishUserEvent(UserEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}
