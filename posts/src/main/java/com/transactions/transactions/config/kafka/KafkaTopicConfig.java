//package com.transactions.transactions.config.kafka;
//
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.kafka.config.TopicBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class KafkaTopicConfig {
//
//    @Value("${spring.kafka.topic.name}")
//    private String topicName;
//
//    @Bean
//    public NewTopic topic() {
//        return TopicBuilder.name(topicName)
//                .build();
//    }
//}
