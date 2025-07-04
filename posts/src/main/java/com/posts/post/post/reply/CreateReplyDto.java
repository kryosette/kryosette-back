package com.posts.post.post.reply;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReplyDto {
    @NotBlank
    private String content;
}
