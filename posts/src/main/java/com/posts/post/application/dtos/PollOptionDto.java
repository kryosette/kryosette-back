package com.posts.post.application.dtos;

import lombok.Data;

@Data
public class PollOptionDto {
    private Long id;
    private String text;
    private Long voteCount;
}
