//package com.example.demo.broker.kafka.producer;
//
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class KafkaProducerService {
//
//    private final KafkaTemplate<String, String> kafkaTemplate;
//
//    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    public void sendOrderEvent(String orderId) {
//        kafkaTemplate.send("order-topic", orderId);
//        System.out.println("Order event sent for order ID: " + orderId);
//    }
//}