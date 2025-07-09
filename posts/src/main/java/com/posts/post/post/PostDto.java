package com.posts.post.post;

import com.posts.post.post.comment.CommentDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private String userId;
    private String authorName;
    private LocalDateTime createdAt;
    private Long likesCount;
    private Boolean isLiked;
    private Long viewsCount;
    private List<CommentDto> comments;
}
