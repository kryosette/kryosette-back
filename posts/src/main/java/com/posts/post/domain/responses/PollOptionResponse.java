package com.posts.post.domain.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PollOptionResponse {
    private Long id;
    private String text;
    private Long voteCount;
    private boolean voted;
}
