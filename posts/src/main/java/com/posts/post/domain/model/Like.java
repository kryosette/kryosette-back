package com.posts.post.domain.model;

//import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "likes")
@IdClass(LikeId.class)
public class Like {

    @Id
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Id
    @JoinColumn(name = "user_id")
    private String userId;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /*
    This is the optimistic locking mechanism in JPA/Hibernate.
    Instead of locking rows in the database for the duration of a transaction (pessimistic locking),
    we allow everyone to read and modify the data, but check whether it has changed when saving.
     */
    @Version
    private Long version;
}

