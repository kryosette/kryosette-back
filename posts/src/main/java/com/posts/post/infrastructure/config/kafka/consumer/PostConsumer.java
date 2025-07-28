//package com.transactions.transactions.config.kafka.consumer;
//
//import com.transactions.transactions.post.events.CreatePostEvent;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class PostConsumer {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(PostConsumer.class);
//
//    public void consume(CreatePostEvent event){
//        LOGGER.info(String.format("Order event received in stock service => %s", event.toString()));
//
//        // save the order event into the database
//    }
//}