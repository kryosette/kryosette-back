package com.posts.post.application.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreatePostDto {
    private String title;
    private String content;
    @FutureOrPresent(message = "Expiration date must be in the future or present")
    @Nullable
    private LocalDateTime expiresAt;

//    // Геттеры и сеттеры
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//    public String getContent() { return content; }
//    public void setContent(String content) { this.content = content; }
}