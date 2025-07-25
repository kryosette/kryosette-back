package com.posts.post.post;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Data
public class PostCreateRequest {
    private String title;
    private String content;
    private String authorId;
    private List<String> imagePaths;
    private List<String> videoPaths;
    private List<String> audioPaths;
    private Set<String> hashtags;

    @Override
    public String toString() {
        return "PostCreateRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                '}';
    }
}