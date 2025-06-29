package com.posts.post.post.like;

import com.posts.post.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
    boolean existsByPostAndUserId(Post post, String userId);
    long countByPost(Post post);
    void deleteByPostAndUserId(Post post, String userId);
}
