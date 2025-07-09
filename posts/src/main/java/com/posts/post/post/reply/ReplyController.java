package com.posts.post.post.reply;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/comments/{commentId}/replies")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    /**
     * Creates a new reply to a specific comment.
     *
     * @param commentId The ID of the comment being replied to.
     * @param dto       The data transfer object containing the reply information.
     * @param authHeader The authorization header containing the user's token.
     * @return ResponseEntity containing the created ReplyDto with HTTP status CREATED.
     * @throws UserPrincipalNotFoundException if the user principal cannot be found based on the token.
     * @throws ResponseStatusException if the authorization header is invalid.
     */
    @PostMapping
    public ResponseEntity<ReplyDto> createReply(
            @PathVariable Long commentId,
            @RequestBody CreateReplyDto dto,
            @RequestHeader("Authorization") String authHeader
    ) throws UserPrincipalNotFoundException {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");

        ReplyDto reply = replyService.createReply(commentId, dto, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    /**
     * Retrieves all replies to a specific comment.
     *
     * @param commentId The ID of the comment.
     * @return ResponseEntity containing a list of ReplyDto objects with HTTP status OK.
     */
    @GetMapping
    public ResponseEntity<List<ReplyDto>> getReplies(@PathVariable Long commentId) {
        return ResponseEntity.ok(replyService.getReplies(commentId));
    }
}