package com.posts.post.post.reply;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByCommentIdOrderByCreatedAtAsc(Long commentId);
    long countByCommentId(Long commentId);
}