//package com.transactions.transactions.config.kafka;
//
//import com.transactions.transactions.config.kafka.events.UserEvent;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Map;
//
//@Configuration
//public class KafkaConfig {
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Bean
//    public ConsumerFactory<String, UserEvent> userEventConsumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "post-service-group");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
//                new JsonDeserializer<>(UserEvent.class));
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> userEventListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(userEventConsumerFactory());
//        return factory;
//    }
//}