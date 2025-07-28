package com.posts.post.domain.repositories;

import com.posts.post.domain.model.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostViewRepository extends JpaRepository<PostView, Long> {
    @Query("SELECT COUNT(pv) FROM PostView pv WHERE pv.postId = :postId")
    Long countByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(DISTINCT pv.userId) FROM PostView pv WHERE pv.postId = :postId")
    Long countUniqueUserViewsByPostId(@Param("postId") Long postId);

    boolean existsByPostIdAndUserId(Long postId, String userId);
}
