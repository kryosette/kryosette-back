//package com.transactions.transactions.post.reply;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/comments/{commentId}/replies")
//@RequiredArgsConstructor
//public class ReplyController {
//    private final ReplyService replyService;
//
//    @PostMapping
//    public ResponseEntity<ReplyDto> createReply(
//            @PathVariable Long postId,
//            @PathVariable Long commentId,
//            @RequestBody CreateReplyDto dto,
//            @AuthenticationPrincipal UserDetails user) {
//
//        dto.setPostId(postId);
//        dto.setParentId(commentId);
//        ReplyDto reply = replyService.createReply(commentId, dto, user.getUsername());
//        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<ReplyDto>> getReplies(
//            @PathVariable Long postId,
//            @PathVariable Long commentId) {
//
//        return ResponseEntity.ok(replyService.getRepliesForComment(commentId));
//    }
//}