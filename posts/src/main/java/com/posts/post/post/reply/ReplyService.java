//package com.transactions.transactions.post.reply;
//
//import com.transactions.transactions.post.comment.Comment;
//import com.transactions.transactions.post.comment.CommentRepository;
//import com.example.demo.user.User;
//import com.example.demo.user.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.webjars.NotFoundException;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ReplyService {
//    private final ReplyRepository replyRepository;
//    private final CommentRepository commentRepository;
//    private final UserRepository userRepository;
//
//    @Transactional
//    public ReplyDto createReply(Long commentId, CreateReplyDto dto, String username) {
//        Comment comment = commentRepository.findById(commentId)
//                .orElseThrow(() -> new NotFoundException("Comment not found"));
//
//        User author = userRepository.findByEmail(username)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//
//        Reply reply = new Reply();
//        reply.setContent(dto.getContent());
//        reply.setParentComment(comment);
//        reply.setAuthor(author);
//
//        Reply saved = replyRepository.save(reply);
//        return convertToDto(saved);
//    }
//
//    public List<ReplyDto> getRepliesForComment(Long commentId) {
//        return replyRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId)
//                .stream()
//                .map(this::convertToDto)
//                .toList();
//    }
//
//    private ReplyDto convertToDto(Reply reply) {
//        return new ReplyDto(
//                reply.getId(),
//                reply.getContent(),
//                Long.parseLong(reply.getAuthor().getId()),
//                reply.getAuthor().getUsername(),
//                reply.getCreatedAt(),
//                reply.getParentComment().getId()
//             );
//    }
//
//}