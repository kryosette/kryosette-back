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

    /**
     * Creates a new reply to a specific comment.  This method verifies the user's
     * token, retrieves user information, finds the associated comment, and saves
     * the reply to the database.
     *
     * @param commentId The ID of the comment being replied to.
     * @param dto       The data transfer object containing the reply information.
     * @param token     The user's authorization token.
     * @return ReplyDto The created reply data transfer object.
     * @throws UserPrincipalNotFoundException if the user information cannot be
     *                                        retrieved from the token.
     * @throws SecurityException              if there is an authorization error.
     * @throws ResourceNotFoundException      if the comment is not found.
     */
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

    /**
     * Retrieves all replies to a specific comment, ordered by creation date
     * ascending.
     *
     * @param commentId The ID of the comment.
     * @return List<ReplyDto> A list of reply data transfer objects for the specified
     * comment.
     */
    public List<ReplyDto> getReplies(Long commentId) {
        return replyRepository.findByCommentIdOrderByCreatedAtAsc(commentId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Reply object to a ReplyDto object.
     *
     * @param reply The Reply object to convert.
     * @return ReplyDto The converted reply data transfer object.
     */
    private ReplyDto convertToDto(Reply reply) {
        ReplyDto dto = new ReplyDto();
        dto.setId(reply.getId());
        dto.setContent(reply.getContent());
        dto.setUsername(reply.getUsername());
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setCommentId(reply.getComment().getId());
        return dto;
    }

    /**
     * Verifies the user's token by sending a request to the authentication service.
     *
     * @param token The user's authorization token.
     * @return Map<String, String> A map containing the user's information (userId,
     * username).
     * @throws SecurityException if there is an authorization error.
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
            throw new SecurityException("Ошибка авторизации");
        }

        return (Map<String, String>) response.getBody();
    }
}