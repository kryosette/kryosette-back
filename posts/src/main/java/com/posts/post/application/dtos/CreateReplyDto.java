package com.posts.post.application.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReplyDto {
    @NotBlank
    private String content;
}
