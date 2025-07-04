package com.posts.post.post;

import com.posts.post.post.like.Like;
import com.posts.post.post.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AuthServiceClient authServiceClient;
    private final RestTemplate restTemplate;
    private final LikeRepository likeRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public CompletableFuture<Post> createPost(PostCreateRequest request, String token) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> authData = verifyTokenAsync(token).get();

                Post post = new Post();
                post.setTitle(request.getTitle());
                post.setContent(request.getContent());
                post.setAuthorId(authData.get("userId"));
                post.setAuthor(authData.get("username"));

                return postRepository.save(post);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

    private CompletableFuture<Map<String, String>> verifyTokenAsync(String token) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token.trim());

            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://auth-service/api/v1/auth/verify",
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SecurityException("Ошибка авторизации");
            }

            return (Map<String, String>) response.getBody();
        }, executorService);
    }

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
}

