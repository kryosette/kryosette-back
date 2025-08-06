package com.posts.post.application.dtos;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PollDto {
    private Long id;
    private String question;
    private boolean multipleChoice;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private List<PollOptionDto> options;
    private Long totalVotes;
}

