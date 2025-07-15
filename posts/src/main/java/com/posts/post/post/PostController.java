package com.posts.post.post;

import com.posts.post.post.like.LikeRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RestController
@RequestMapping("posts")
@RequiredArgsConstructor
@Tag(name = "Posts")
/**
 * REST controller for managing blog posts, including creation, deletion, and like operations.
 * All endpoints requiring authentication expect an opaque Bearer token in the Authorization header.
 */
public class PostController {

    Logger log = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final AuthServiceClient authServiceClient;

    /**
     * Creates a new blog post after validating the authentication token
     *
     * @param request DTO containing post title and content
     * @param authHeader Authorization header containing Bearer token (format: "Bearer {token}")
     * @return CompletableFuture containing the created Post
     * @throws ResponseStatusException with UNAUTHORIZED (401) if token is invalid or missing user claims
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR (500) for other server errors
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(
            @RequestBody @Valid PostCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }
        String token = authHeader.replace("Bearer ", "");

        try {
            return postService.createPost(request, token);
        } catch (UserPrincipalNotFoundException | SecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при создании поста: " + e.getMessage()
            );
        }
    }

    /**
     * Retrieves paginated list of all posts in descending order of creation time
     *
     * @param page page number (default: 0)
     * @param size number of items per page (default: 10 for first page, 5 for subsequent pages)
     * @return ResponseEntity containing Page of PostDto objects
     */
    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int actualSize = (page == 0) ? 10 : 5;
        Pageable pageable = PageRequest.of(page, actualSize, Sort.by("createdAt").descending());

        Page<PostDto> postPage = postService.getAllPosts(pageable)
                .map(this::mapPostToDto);

        return ResponseEntity.ok(postPage);
    }

    /**
     * Deletes a specific post after verifying authorization
     *
     * @param authHeader Authorization header containing Bearer token
     * @param postId ID of the post to delete
     * @return ResponseEntity with no content (204) on success
     * @throws ResponseStatusException with UNAUTHORIZED (401) for invalid/missing token
     * @throws ResponseStatusException with NOT_FOUND (404) if post doesn't exist
     * @throws ResponseStatusException with FORBIDDEN (403) if user lacks deletion permission
     */
    @DeleteMapping("/{postId}/delete")
    public ResponseEntity<?> deletePost(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer postId) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }
        String token = authHeader.replace("Bearer ", "");

        try {
            postService.deletePost(token, postId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        } catch (AccessDeniedException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this post");
        } catch (Exception ex) {
            log.error("Error deleting post", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while deleting the post");
        }
    }

    /**
     * Toggles a like on a post for the authenticated user
     *
     * @param authHeader Authorization header containing Bearer token
     * @param postId ID of the post to like/unlike
     * @return ResponseEntity with like status (true if liked, false if unliked)
     * @throws ResponseStatusException with UNAUTHORIZED (401) for invalid/missing token
     * @throws UserPrincipalNotFoundException if user ID cannot be extracted from token
     */
    @Transactional
    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createLike(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer postId
    ) throws UserPrincipalNotFoundException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");
        ResponseEntity<?> like = postService.createLike(token, postId);
        return ResponseEntity.ok(like).getBody();
    }

    /**
     * Gets the total like count for a specific post
     *
     * @param postId ID of the post
     * @return ResponseEntity containing the like count
     * @throws RuntimeException if post is not found
     */
    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<Long> getLikesCount(@PathVariable Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return ResponseEntity.ok(likeRepository.countByPost(post));
    }

    /**
     * Checks if the authenticated user has liked a specific post
     *
     * @param postId ID of the post to check
     * @param authHeader Authorization header containing Bearer token
     * @return ResponseEntity with boolean indicating like status
     * @throws ResponseStatusException with UNAUTHORIZED (401) for invalid/missing token
     * @throws UserPrincipalNotFoundException if user ID cannot be extracted from token
     */
    @GetMapping("/{postId}/likes/check")
    public ResponseEntity<Boolean> checkIfLiked(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader
    ) throws UserPrincipalNotFoundException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");
        ResponseEntity<?> checkIfLike = postService.checkIfLike(token, postId);
        return (ResponseEntity<Boolean>) ResponseEntity.ok(checkIfLike).getBody();
    }

    /**
     * Retrieves a post with its comments (cached)
     *
     * @param postId ID of the post to retrieve
     * @return ResponseEntity containing PostDto with comments
     * @throws RuntimeException if post is not found
     */
    @Cacheable(value = "postsWithComments", key = "#postId")
    @GetMapping("/{postId}/with-comments")
    public ResponseEntity<PostDto> getPostWithComments(
            @PathVariable Long postId
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostDto postDto = mapPostToDto(post);
        return ResponseEntity.ok(postDto);
    }

    /**
     * Maps Post entity to PostDto
     *
     * @param post the Post entity to convert
     * @return PostDto containing essential post information
     */
    private PostDto mapPostToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setAuthorName(post.getAuthor());
        dto.setHashtags(post.getHashtags());

        Map<String, Long> viewStats = postService.getPostViewStats(post.getId());
        dto.setViewsCount(viewStats.get("totalViews"));

        return dto;
    }

    /**
     * Records a post view
     */
    @PostMapping("/{postId}/view")
    public ResponseEntity<?> recordPostView(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            postService.recordPostView(token, postId, request);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Gets post view statistics
     */
    @GetMapping("/{postId}/views/stats")
    public ResponseEntity<Map<String, Long>> getPostViewStats(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostViewStats(postId));
    }

    @GetMapping("/hashtags/popular")
    public ResponseEntity<List<String>> getPopularHashtags(
            @RequestParam(defaultValue = "10") int count) {
        List<String> popularHashtags = postService.getPopularHashtags(count);
        return ResponseEntity.ok(popularHashtags);
    }

    @GetMapping("/hashtags/{hashtag}")
    public ResponseEntity<Page<PostDto>> getPostsByHashtag(
            @PathVariable String hashtag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostDto> posts = postService.getPostsByHashtag(hashtag, pageable)
                .map(this::mapPostToDto);

        return ResponseEntity.ok(posts);
    }

}