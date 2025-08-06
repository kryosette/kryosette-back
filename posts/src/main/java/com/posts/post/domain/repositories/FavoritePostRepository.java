//package com.posts.post.domain.repositories;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//public interface FavoritePostRepository extends JpaRepository<FavoritePost, Long> {
//    boolean existsByPostIdAndUserId(Long postId, String userId);
//
//    void deleteByPostIdAndUserId(Long postId, String userId);
//
//    Page<FavoritePost> findByUserId(String userId, Pageable pageable);
//
//    @Query("SELECT fp.postId FROM FavoritePost fp WHERE fp.userId = :userId")
//    Page<Long> findPostIdsByUserId(@Param("userId") String userId, Pageable pageable);
//}