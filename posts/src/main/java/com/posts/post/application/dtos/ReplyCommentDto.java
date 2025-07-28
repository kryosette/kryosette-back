package com.posts.post.application.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCommentDto {
    @NotBlank
    private String content;
    private Long parentCommentId;
}