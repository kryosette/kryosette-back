package com.posts.post.post.comment;

import com.posts.post.post.Post;
import com.posts.post.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Transactional
    public CommentDto createComment(
            CreateCommentDto commentDto,
            String token
    ) throws UserPrincipalNotFoundException {
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
        String username = (String) Objects.requireNonNull(response.getBody().get("username"));
        if (userId == null) {
            throw new UserPrincipalNotFoundException("User ID не найден в токене");
        }

        Post post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setPost(post);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setIsPinned(false);

        Comment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }

    public List<CommentDto> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public long getCommentsCountByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentRepository.countByPost(post);
    }

    private CommentDto convertToDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getPost().getId(),
                comment.getUserId(),
                comment.getUsername(),
                comment.getCreatedAt(),
                comment.getIsPinned()
        );
    }

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
            throw new SecurityException("Ошибка авторизации");
        }

        return (Map<String, String>) response.getBody();
    }

    public Comment findById(Long commentId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        return commentOptional.orElseThrow(() ->
                new RuntimeException("Comment not found with id: " + commentId));
    }

    @Transactional
    public CommentDto pinComment(Long postId, Long commentId, String currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Comment does not belong to this post");
        }

        if (Boolean.TRUE.equals(comment.getIsPinned())) {
            comment.setIsPinned(false);
        } else {
            commentRepository.unpinAllCommentsInPost(postId);
            comment.setIsPinned(true);
        }

        Comment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }
}