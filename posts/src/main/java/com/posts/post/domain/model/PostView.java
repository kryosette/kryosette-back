package com.posts.post.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_views")
@Getter
@Setter
@NoArgsConstructor
public class PostView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    public PostView(Long postId, String userId) {
        this.postId = postId;
        this.userId = userId;
        this.viewedAt = LocalDateTime.now();
    }
}