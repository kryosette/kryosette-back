package com.posts.post.domain.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
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
    private PollCreateRequest poll;
    @Nullable
    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "UTC")
    private LocalDateTime expiresAt;

    @Override
    public String toString() {
        return "PostCreateRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                '}';
    }
}

