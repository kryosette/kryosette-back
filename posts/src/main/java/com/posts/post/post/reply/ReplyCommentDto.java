package com.posts.post.post.reply;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCommentDto {
    @NotBlank
    private String content;
    private Long parentCommentId;
}