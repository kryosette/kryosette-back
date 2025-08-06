package com.posts.post.domain.services;

import com.posts.post.application.dtos.CreateReplyDto;
import com.posts.post.application.dtos.ReplyDto;
import com.posts.post.domain.aspect.GetToken;
import com.posts.post.domain.model.Comment;
import com.posts.post.domain.model.Reply;
import com.posts.post.domain.repositories.CommentRepository;
import com.posts.post.domain.repositories.ReplyRepository;
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
    private final GetToken getToken;

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
        String userId = getToken.verifyTokenAndGetUserId(token);
        String email = getToken.verifyTokenAndGetEmail(token);

        if (userId == null) {
            throw new UserPrincipalNotFoundException("User ID не найден в токене");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Reply reply = new Reply();
        reply.setContent(dto.getContent());
        reply.setComment(comment);
        reply.setUserId(userId);
        reply.setUsername(email);

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
}