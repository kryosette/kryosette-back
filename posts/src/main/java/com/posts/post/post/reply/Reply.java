//package com.transactions.transactions.post.reply;
//
//
//import com.transactions.transactions.post.comment.Comment;
//import com.example.demo.user.User;
//import jakarta.persistence.*;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.CreationTimestamp;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "replies")
//@EntityListeners(AuditingEntityListener.class)
//public class Reply {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(columnDefinition = "TEXT")
//    private String content;
//
//    @ManyToOne
//    @JoinColumn(name = "comment_id", nullable = false)
//    private Comment parentComment;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User author;
//
//    @Column(name = "created_at", nullable = false)
//    @CreationTimestamp
//    private LocalDateTime createdAt;
//}