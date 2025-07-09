package com.posts.post.post.poll;

import lombok.Data;
import java.util.List;

@Data
public class PollCreationRequest {
    private String question;
    private List<String> options;
}

@Data
class PollResponse {
    private Long id;
    private String question;
    private List<OptionResponse> options;
}

@Data
class OptionResponse {
    private Long id;
    private String text;
    private int votes;
}