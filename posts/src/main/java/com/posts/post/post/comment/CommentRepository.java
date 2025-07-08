package com.posts.post.post.comment;

import com.posts.post.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(Long postId);
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    long countByPost(Post post);
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Comment c SET c.isPinned = false WHERE c.post.id = :postId")
    void unpinAllCommentsInPost(@Param("postId") Long postId);
}