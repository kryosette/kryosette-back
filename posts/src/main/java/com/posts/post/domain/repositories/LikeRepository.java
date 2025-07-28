package com.posts.post.domain.repositories;

import com.posts.post.domain.model.Like;
import com.posts.post.domain.model.LikeId;
import com.posts.post.domain.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
    @Modifying
    @Query("DELETE FROM Like l WHERE l.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    boolean existsByPostAndUserId(Post post, String userId);
    long countByPost(Post post);
    void deleteByPostAndUserId(Post post, String userId);
}
