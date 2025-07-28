package com.posts.post.application.controllers;

import com.posts.post.domain.annotations.ExtractAuthorizationToken;
import com.posts.post.domain.model.Comment;
import com.posts.post.application.dtos.CommentDto;
import com.posts.post.domain.services.CommentService;
import com.posts.post.application.dtos.CreateCommentDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments")
public class CommentController {
    private final CommentService commentService;

    /**
     * Creates a new comment for a specific post.
     *
     * @param postId      The ID of the post to which the comment belongs.
     * @param commentDto  The data transfer object containing the comment information.
     * @param authHeader  The authorization header containing the user's token.
     * @return ResponseEntity containing the created CommentDto with HTTP status CREATED.
     * @throws UserPrincipalNotFoundException if the user principal cannot be found based on the token.
     * @throws ResponseStatusException if the authorization header is invalid.
     */
    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentDto commentDto,
            @ExtractAuthorizationToken String token
    ) throws UserPrincipalNotFoundException {
        commentDto.setPostId(postId);
        CommentDto createdComment = commentService.createComment(commentDto, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * Maps a Comment object to a CommentDto object.
     *
     * @param comment The Comment object to map.
     * @return A CommentDto object representing the Comment.
     */
    private CommentDto mapCommentToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUserId(comment.getUserId());
        return dto;
    }

    /**
     * Retrieves all comments for a given post ID.
     *
     * @param postId The ID of the post.
     * @return ResponseEntity containing a list of CommentDto objects with HTTP status OK.
     */
    @GetMapping
    public ResponseEntity<List<CommentDto>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    /**
     * Retrieves the total number of comments for a specific post.
     *
     * @param postId The ID of the post.
     * @return ResponseEntity containing the number of comments with HTTP status OK.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getCommentsCountByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsCountByPostId(postId));
    }

    /**
     * Toggles the pinned status of a comment.
     *
     * @param postId    The ID of the post the comment belongs to.
     * @param commentId The ID of the comment to pin/unpin.
     * @param authHeader The authorization header containing the user's token.
     * @return ResponseEntity containing the updated CommentDto with HTTP status OK.
     * @throws ResponseStatusException if the authorization header is invalid.
     */
    @PatchMapping("/{commentId}/pin")
    public ResponseEntity<CommentDto> togglePinComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @ExtractAuthorizationToken String token
    ) {
        CommentDto updatedComment = commentService.pinComment(postId, commentId, token);
        return ResponseEntity.ok(updatedComment);
    }

}