package com.posts.post.domain.requests;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PollRequest {
    @NotBlank
    private String question;

    @Size(min = 2, max = 10)
    private List<@NotBlank String> options;

    private boolean multipleChoice;

    @Future
    private LocalDateTime expiresAt;
}
