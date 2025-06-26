package com.transactions.transactions.post.like;

import com.transactions.transactions.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
    boolean existsByPostAndUserId(Post post, String userId);
    long countByPost(Post post);
    void deleteByPostAndUserId(Post post, String userId);
}
