package com.posts.post.domain.services;

import com.posts.post.application.dtos.CommentDto;
import com.posts.post.domain.aspect.GetToken;
import com.posts.post.domain.model.Comment;
import com.posts.post.domain.model.Post;
import com.posts.post.domain.repositories.CommentRepository;
import com.posts.post.domain.repositories.PostRepository;
import com.posts.post.application.dtos.CreateCommentDto;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final RestTemplate restTemplate;
    private final GetToken getToken;

    /**
     * Creates a new comment for a specific post.
     * This method verifies the user's token, retrieves user information,
     * finds the associated post, and saves the comment to the database.
     *
     * @param commentDto The data transfer object containing the comment information.
     * @param token      The user's authorization token.
     * @return CommentDto The created comment data transfer object.
     * @throws UserPrincipalNotFoundException if the user information cannot be retrieved from the token.
     * @throws SecurityException if there is an authorization error.
     * @throws RuntimeException if the post is not found.
     */
    @Transactional
    public CommentDto createComment(
            CreateCommentDto commentDto,
            String token
    ) throws UserPrincipalNotFoundException {
        String userId = getToken.verifyTokenAndGetUserId(token);
        String email = getToken.verifyTokenAndGetEmail(token);

        if (userId == null) {
            throw new UserPrincipalNotFoundException("User ID не найден в токене");
        }

        Post post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setPost(post);
        comment.setUserId(userId);
        comment.setUsername(email);
        comment.setIsPinned(false);

        Comment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }

    /**
     * Retrieves all comments associated with a specific post, ordered by creation date descending.
     *
     * @param postId The ID of the post.
     * @return List<CommentDto> A list of comment data transfer objects for the specified post.
     */
    public List<CommentDto> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the total number of comments for a given post.
     *
     * @param postId The ID of the post.
     * @return long The number of comments for the post.
     * @throws RuntimeException if the post is not found.
     */
    public long getCommentsCountByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentRepository.countByPost(post);
    }

    /**
     * Converts a Comment object to a CommentDto object.
     *
     * @param comment The Comment object to convert.
     * @return CommentDto The converted comment data transfer object.
     */
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

    /**
     * Finds a comment by its ID.
     *
     * @param commentId The ID of the comment to find.
     * @return Comment The Comment object.
     * @throws RuntimeException if the comment is not found.
     */
    public Comment findById(Long commentId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        return commentOptional.orElseThrow(() ->
                new RuntimeException("Comment not found with id: " + commentId));
    }

    /**
     * Toggles the pinned status of a comment.
     *
     * @param postId        The ID of the post the comment belongs to.
     * @param commentId     The ID of the comment to pin/unpin.
     * @param currentUserId The ID of the user performing the action.
     * @return CommentDto The updated comment data transfer object.
     * @throws RuntimeException if the post or comment is not found, or if the comment does not belong to the post.
     */
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