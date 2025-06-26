//package com.transactions.transactions.post.reply;
//
//import com.transactions.transactions.post.comment.Comment;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface ReplyRepository extends JpaRepository<Reply, Long> {
//    List<Reply> findByParentCommentIdOrderByCreatedAtAsc(Long commentId);
//    long countByParentComment(Comment comment);
//}