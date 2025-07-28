package com.posts.post.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
    private Long likesCount;
    private Boolean isLiked;
    private Long viewsCount;
    private List<CommentDto> comments;
    private Set<String> hashtags;
}
