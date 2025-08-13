package com.posts.post.domain.services;

import com.posts.post.domain.aspect.GetToken;
import com.posts.post.domain.model.*;
import com.posts.post.domain.repositories.*;
import com.posts.post.domain.requests.PostCreateRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.AccessDeniedException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final GetToken getToken;
    private final PollService pollService;
    private final PollOptionRepository pollOptionRepository;
    private final PollRepository pollRepository;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    /**
     * Retrieves all posts in descending order of creation time
     *
     * @param pageable pagination information (page number, size, etc.)
     * @return Page object containing posts and pagination details
     */
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAllActive(pageable);
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
        String userId = getToken.verifyTokenAndGetUserId(token);
        String email = getToken.verifyTokenAndGetEmail(token);

        if (userId == null || email == null) {
            throw new UserPrincipalNotFoundException("User credentials not found in token");
        }

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthorId(userId);
        post.setAuthor(email);
        post.setExpiresAt(request.getExpiresAt());

        if (request.getHashtags() != null) {
            Set<String> normalizedHashtags = request.getHashtags().stream()
                    .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
                    .collect(Collectors.toSet());
            post.setHashtags(normalizedHashtags);
        }

        Post savedPost = postRepository.save(post);

        // Create poll if included in request
        if (request.getPoll() != null) {
            Poll poll = new Poll();
            poll.setPost(savedPost);
            poll.setQuestion(request.getPoll().getQuestion());
            poll.setMultipleChoice(request.getPoll().isMultipleChoice());

            if (request.getPoll().getExpiresAt() != null) {
                poll.setExpiresAt(request.getPoll().getExpiresAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
            }

            Poll savedPoll = pollRepository.save(poll);

            List<PollOption> options = request.getPoll().getOptions().stream()
                    .map(optionText -> {
                        PollOption option = new PollOption();
                        option.setPoll(savedPoll);
                        option.setText(optionText);
                        return option;
                    })
                    .collect(Collectors.toList());

            pollOptionRepository.saveAll(options);
            savedPoll.setOptions(options);
        }

        return savedPost;
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
        String userId = getToken.verifyTokenAndGetUserId(token);

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
        Post post = postRepository.findById(Long.valueOf(postId))
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String userId = getToken.verifyTokenAndGetUserId(token);

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

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String userId = getToken.verifyTokenAndGetUserId(token);

        if (userId == null) {
            throw new UserPrincipalNotFoundException("User ID не найден в токене");
        }

        return ResponseEntity.ok(likeRepository.existsByPostAndUserId(post, userId));
    }

    @Transactional
    public void recordPostView(String token, Long postId, HttpServletRequest request) {
        String userId = getToken.verifyTokenAndGetUserId(token);

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

    /*
    * spring.data.web.pageable.max-page-size = 100 (safe)
    * */
    @Override
    public Page<String> getPopularHashtags(int count) {
        LocalDateTime dateFrom = LocalDateTime.now();

        Pageable pageable = PageRequest.of(0, count); // safe
        return postRepository.findPopularHashtags(dateFrom, pageable);
    }

    @Override
    public Optional<Post> getPost(Long id) {
                return postRepository.findActiveById(id);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredPosts() {
        log.info("Cleaning up expired posts...");
        int deletedCount = postRepository.deleteExpiredPosts();
        log.info("Deleted {} expired posts", deletedCount);
    }

}
