package com.transactions.transactions.post.comment;

import com.transactions.transactions.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(Long postId);
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    long countByPost(Post post);
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
}