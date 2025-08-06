package com.posts.post.domain.repositories;

import com.posts.post.domain.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"poll"}) // Only fetch poll eagerly
    @Query("SELECT p FROM Post p")
    Page<Post> findAllWithPoll(Pageable pageable);

    @Query("""
    SELECT p FROM Post p 
    WHERE EXISTS (
        SELECT h FROM p.hashtags h 
        WHERE h = :hashtag
    )
    ORDER BY p.createdAt DESC
    """)
    Page<Post> findByHashtagsContaining(@Param("hashtag") String hashtag, Pageable pageable);

    @Query("""
    SELECT ph.hashtag 
    FROM PostHashtag ph 
    WHERE ph.createdAt >= :dateFrom
    GROUP BY ph.hashtag
    ORDER BY COUNT(ph) DESC
    """)
    Page<String> findPopularHashtags(@Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP) ORDER BY p.createdAt DESC")
    Page<Post> findAllActive(Pageable pageable);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.expiresAt IS NOT NULL AND p.expiresAt <= CURRENT_TIMESTAMP")
    int deleteExpiredPosts();

    @Query("SELECT p FROM Post p WHERE p.id = :id AND (p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP)")
    Optional<Post> findActiveById(@Param("id") Long id);
}

