package com.posts.post.application.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostDto {
    private String title;
    private String content;

//    // Геттеры и сеттеры
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//    public String getContent() { return content; }
//    public void setContent(String content) { this.content = content; }
}