package com.transactions.transactions.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

//    @Query("SELECT p FROM Post p ORDER BY p.likes DESC, p.createdAt DESC")
//    List<Post> findAllByOrderByLikesDescAndCreatedAtDesc();
}