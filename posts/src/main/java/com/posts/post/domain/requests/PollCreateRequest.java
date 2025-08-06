package com.posts.post.domain.requests;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Data
public class PollCreateRequest {
    private String question;
    private List<String> options;
    private boolean multipleChoice;
    private LocalDateTime expiresAt;
    // getters and setters
}
