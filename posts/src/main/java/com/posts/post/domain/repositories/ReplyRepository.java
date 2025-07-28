package com.posts.post.domain.repositories;

import com.posts.post.domain.model.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByCommentIdOrderByCreatedAtAsc(Long commentId);
    long countByCommentId(Long commentId);
}