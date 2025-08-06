package com.posts.post.domain.responses;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PollResponse {
    private Long id;
    private String question;
    private List<PollOptionResponse> options;
    private boolean multipleChoice;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean voted;
    private Long totalVotes;
}

