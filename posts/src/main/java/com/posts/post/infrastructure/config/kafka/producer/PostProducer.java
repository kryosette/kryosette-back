//package com.transactions.transactions.config.kafka.producer;
//
//import com.transactions.transactions.post.events.CreatePostEvent;
//import lombok.RequiredArgsConstructor;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.stereotype.Service;
//
//
//@Service
//@RequiredArgsConstructor
//public class PostProducer {
//    private static final Logger LOGGER = LoggerFactory.getLogger(PostProducer.class);
//
//    private final NewTopic topic;
//
//    private final KafkaTemplate<String, CreatePostEvent> kafkaTemplate;
//
//    public void sendMessage(CreatePostEvent event){
//        LOGGER.info(String.format("Order event => %s", event.toString()));
//
//        // create Message
//        Message<CreatePostEvent> message = MessageBuilder
//                .withPayload(event)
//                .setHeader(KafkaHeaders.TOPIC, topic.name())
//                .build();
//        kafkaTemplate.send(message);
//    }
//}
