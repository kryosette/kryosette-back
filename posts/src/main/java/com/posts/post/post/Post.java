package com.posts.post.post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    private List<String> fileIds;

    @JoinColumn(name = "user_id", nullable = false)
    private String author;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ElementCollection
    private List<String> fileUrls;

    public Post(String title, String content, Long id) {
        this.title = title;
        this.content = content;
        this.id = id;
    }


    public Long getAuthorId() {
        this.id = id;
        return null;
    }

    public String getAuthorUsername() {
        this.id = id;
        return null;
    }

    public void setAuthorId(String author) {
        this.author = author;
    }
}
