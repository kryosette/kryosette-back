package com.posts.post.post.reply;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyDto {
    private Long id;
    private String content;
    private String username;
    private LocalDateTime createdAt;
    private Long commentId;
}