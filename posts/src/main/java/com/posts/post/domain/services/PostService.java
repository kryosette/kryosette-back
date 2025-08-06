package com.posts.post.domain.services;

import com.posts.post.application.dtos.PostDto;
import com.posts.post.domain.model.Post;
import com.posts.post.domain.requests.PostCreateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.nio.file.AccessDeniedException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PostService {
    Page<Post> getAllPosts(Pageable pageable);

    Post createPost(PostCreateRequest request, String token) throws UserPrincipalNotFoundException;

    void deletePost(String token, Integer postId)
            throws AccessDeniedException, UserPrincipalNotFoundException, ResourceNotFoundException;

    ResponseEntity<?> createLike(String token, Integer postId) throws UserPrincipalNotFoundException;

    ResponseEntity<?> checkIfLike(String token, Long postId) throws UserPrincipalNotFoundException;

    void recordPostView(String token, Long postId, HttpServletRequest request);

    Map<String, Long> getPostViewStats(Long postId);

    Page<Post> getPostsByHashtag(String hashtag, Pageable pageable);

    Page<String> getPopularHashtags(int count);

    Optional<Post> getPost(Long id);
}