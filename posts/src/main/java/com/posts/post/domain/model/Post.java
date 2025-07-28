package com.posts.post.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_author", columnList = "author"),
        @Index(name = "idx_posts_created_at", columnList = "created_at"),
        @Index(name = "idx_posts_hashtags", columnList = "hashtag") // для таблицы post_hashtags
})
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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"),
            indexes = @Index(name = "idx_post_hashtags_hashtag", columnList = "hashtag"))
    @Column(name = "hashtag")
    private Set<String> hashtags = new HashSet<>();

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
