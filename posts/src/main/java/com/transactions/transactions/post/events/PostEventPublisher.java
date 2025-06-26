package com.transactions.transactions.post.events;

import com.transactions.transactions.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostEventPublisher {
    private final KafkaTemplate<String, PostEvent> kafkaTemplate;

    public void publishPostCreated(Post post) {
        PostEvent event = new PostEvent(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorId(),
                post.getAuthorUsername(),
                post.getCreatedAt()
        );
        kafkaTemplate.send("post-events", event);
    }
}
