package com.posts.post.domain.repositories;

import com.posts.post.domain.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
    SELECT p FROM Post p 
    WHERE EXISTS (
        SELECT h FROM p.hashtags h 
        WHERE h = :hashtag
    )
    ORDER BY p.createdAt DESC
    """)
    Page<Post> findByHashtagsContaining(@Param("hashtag") String hashtag, Pageable pageable);

    @Query(nativeQuery = true, value = """
        SELECT hashtag FROM (
            SELECT hashtag, COUNT(*) as cnt 
            FROM post_hashtags
            WHERE created_at >= :dateFrom
            GROUP BY hashtag
            ORDER BY cnt DESC
            LIMIT :limit
        ) t
        """)
    List<String> findPopularHashtags(
            Pageable pageable);
}