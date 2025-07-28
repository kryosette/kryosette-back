package com.posts.post.domain.services;

import com.posts.post.domain.model.Post;
import com.posts.post.domain.model.PostView;
import com.posts.post.domain.repositories.PostRepository;
import com.posts.post.domain.repositories.PostViewRepository;
import com.posts.post.domain.requests.PostCreateRequest;
import com.posts.post.infrastructure.config.AuthServiceClient;
import com.posts.post.domain.repositories.CommentRepository;
import com.posts.post.domain.model.Like;
import com.posts.post.domain.repositories.LikeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.AccessDeniedException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final RestTemplate restTemplate;
    private final LikeRepository likeRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    /**
     * Retrieves all posts in descending order of creation time
     *
     * @param pageable pagination information (page number, size, etc.)
     * @return Page object containing posts and pagination details
     */
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * Creates a new post after verifying the user's authentication token
     * Uses asynchronous token verification with opaque tokens (JWT or similar)
     *
     * @param request post creation DTO containing title and content
     * @param token opaque authentication token (Bearer token)
     * @return CompletableFuture containing the created post
     * @throws CompletionException if token verification fails or user credentials are invalid
     */
    @Transactional
    public Post createPost(PostCreateRequest request, String token) throws UserPrincipalNotFoundException {
        Map<String, String> authData = verifyToken(token);

        if (authData.get("userId") == null || authData.get("username") == null) {
            throw new UserPrincipalNotFoundException("User credentials not found in token");
        }

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthorId(authData.get("userId"));
        post.setAuthor(authData.get("username"));

        if (request.getHashtags() != null) {
            Set<String> normalizedHashtags = request.getHashtags().stream()
                    .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
                    .collect(Collectors.toSet());
            post.setHashtags(normalizedHashtags);
        }

        return postRepository.save(post);
    }

    /**
     * Asynchronously verifies an opaque authentication token with the auth service
     *
     * @param token opaque authentication token to verify
     * @return CompletableFuture containing user claims if verification succeeds
     * @throws CompletionException if token verification fails
     */
    private Map<String, String> verifyToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());

        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Authentication failed: " + response.getStatusCode());
        }

        Map<String, String> body = response.getBody();
        if (body == null) {
            throw new SecurityException("Empty response from auth service");
        }

        return body;
    }

    /**
     * Deletes a post after verifying the user's authentication token
     * Also deletes all associated likes and comments
     *
     * @param token opaque authentication token for authorization
     * @param postId ID of the post to delete
     * @throws AccessDeniedException if user is not authorized to delete the post
     * @throws UserPrincipalNotFoundException if user ID cannot be extracted from token
     * @throws ResourceNotFoundException if post with given ID doesn't exist
     */
    @Transactional
    public void deletePost(String token, Integer postId)
            throws AccessDeniedException, UserPrincipalNotFoundException {
        // Verify token and get user ID
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());

        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
        if (userId == null) {
            throw new UserPrincipalNotFoundException("User ID not found in token");
        }

        Post post = postRepository.findById(Long.valueOf(postId))
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        log.info("Attempting to delete post {} by user {}", postId, userId);

        likeRepository.deleteByPostId(post.getId());

        commentRepository.deleteByPostId(post.getId());

        postRepository.delete(post);
    }

    /**
     * Toggles a like on a post after verifying the user's authentication token
     *
     * @param token opaque authentication token for authorization
     * @param postId ID of the post to like/unlike
     * @return ResponseEntity with liked status (true if like was added, false if removed)
     * @throws UserPrincipalNotFoundException if user ID cannot be extracted from token
     * @throws SecurityException if token verification fails
     */
    @Transactional
    public ResponseEntity<?> createLike(
            String token,
            Integer postId
    ) throws UserPrincipalNotFoundException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());

        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        Post post = postRepository.findById(Long.valueOf(postId))
                .orElseThrow(() -> new RuntimeException("Post not found"));


        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Ошибка авторизации: " + response.getBody());
        }

        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
        if (userId == null) {
            throw new UserPrincipalNotFoundException("User ID не найден в токене");
        }

        if (likeRepository.existsByPostAndUserId(post, userId)) {
            likeRepository.deleteByPostAndUserId(post, userId);
            return ResponseEntity.ok(Map.of("liked", false));
        } else {
            Like like = new Like();
            like.setPost(post);
            like.setUserId(userId);
            likeRepository.save(like);
            return ResponseEntity.ok(Map.of("liked", true));
        }
    }

    /**
     * Checks if the authenticated user has liked a specific post
     *
     * @param token opaque authentication token for authorization
     * @param postId ID of the post to check
     * @return ResponseEntity with boolean indicating like status
     * @throws UserPrincipalNotFoundException if user ID cannot be extracted from token
     * @throws SecurityException if token verification fails
     */
    @Transactional
    public ResponseEntity<?> checkIfLike(
            String token,
            Long postId
    ) throws UserPrincipalNotFoundException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());

        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));


        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Ошибка авторизации: " + response.getBody());
        }

        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
        if (userId == null) {
            throw new UserPrincipalNotFoundException("User ID не найден в токене");
        }

        return ResponseEntity.ok(likeRepository.existsByPostAndUserId(post, userId));
    }

    @Transactional
    public void recordPostView(String token, Long postId, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.trim());

        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/auth/verify",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SecurityException("Ошибка авторизации: " + response.getBody());
        }

        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");

        // Record view only if user hasn't viewed this post before
        if (!postViewRepository.existsByPostIdAndUserId(postId, userId)) {
            PostView view = new PostView(postId, userId);
            postViewRepository.save(view);
        }
    }

    /**
     * Gets view statistics for a post
     */
    public Map<String, Long> getPostViewStats(Long postId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalViews", postViewRepository.countByPostId(postId));
        stats.put("uniqueUserViews", postViewRepository.countUniqueUserViewsByPostId(postId));
        return stats;
    }

    public Page<Post> getPostsByHashtag(String hashtag, Pageable pageable) {
        String normalizedHashtag = hashtag.startsWith("#") ? hashtag : "#" + hashtag;
        return postRepository.findByHashtagsContaining(normalizedHashtag, pageable);
    }

    public List<String> getPopularHashtags(int count) {
        return postRepository.findPopularHashtags(PageRequest.of(0, count));
    }
}
