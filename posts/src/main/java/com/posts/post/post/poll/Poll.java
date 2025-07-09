package com.posts.post.post.poll;

import com.posts.post.post.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "poll")
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "question")
    private String question;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    private List<PollOption> options;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}

@Getter
@Setter
@Entity
@Table(name = "poll_option") // Добавьте Table аннотацию
class PollOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Укажите имя столбца для ID, если нужно
    private Long id;

    @Column(name = "text") // Добавьте Column аннотацию
    private String text;

    @Column(name = "votes") // Добавьте Column аннотацию
    private int votes = 0;

    @ManyToOne  // Отношение: Вариант ответа принадлежит одному опросу
    @JoinColumn(name = "poll_id") // Внешний ключ к таблице poll
    private Poll poll;
}