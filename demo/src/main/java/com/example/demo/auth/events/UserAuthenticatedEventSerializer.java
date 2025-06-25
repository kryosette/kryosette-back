package com.example.demo.auth.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class UserAuthenticatedEventSerializer implements Serializer<UserAuthenticatedEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Do nothing because we're configuring ourselves in the constructor
    }

    @Override
    public byte[] serialize(String topic, UserAuthenticatedEvent data) {
        if (data == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing UserAuthenticatedEvent to JSON", e);
        }
    }

    @Override
    public void close() {
        // Nothing to do
    }
}