package com.posts.post.post.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostEvent {
    private String postId;
    private String title;
    private String content;
    private String userId;
}


