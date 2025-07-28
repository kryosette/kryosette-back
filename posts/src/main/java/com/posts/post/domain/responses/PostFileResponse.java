package com.posts.post.domain.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostFileResponse {
    private String filename;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
}