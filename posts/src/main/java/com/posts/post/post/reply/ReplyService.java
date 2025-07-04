package com.posts.post.post.reply;

import com.posts.post.post.comment.Comment;
import com.posts.post.post.comment.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyService {
    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Transactional
    public ReplyDto createReply(Long commentId, CreateReplyDto dto, String token) throws UserPrincipalNotFoundException {
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

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Reply reply = new Reply();
        reply.setContent(dto.getContent());
        reply.setComment(comment);
        reply.setUserId(userId);
        reply.setUsername(username);

        Reply savedReply = replyRepository.save(reply);
        return convertToDto(savedReply);
    }

    public List<ReplyDto> getReplies(Long commentId) {
        return replyRepository.findByCommentIdOrderByCreatedAtAsc(commentId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ReplyDto convertToDto(Reply reply) {
        ReplyDto dto = new ReplyDto();
        dto.setId(reply.getId());
        dto.setContent(reply.getContent());
        dto.setUsername(reply.getUsername());
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setCommentId(reply.getComment().getId());
        return dto;
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
}