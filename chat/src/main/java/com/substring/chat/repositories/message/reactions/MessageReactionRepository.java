package com.substring.chat.repositories.message.reactions;

import com.substring.chat.domain.model.messages.reactions.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {
    List<MessageReaction> findByMessageId(Long messageId);

    @Query("SELECT mr.reaction, COUNT(mr) FROM MessageReaction mr WHERE mr.message.id = :messageId GROUP BY mr.reaction")
    List<Object[]> countReactionsByMessageId(@Param("messageId") Long messageId);

    @Query("SELECT mr FROM MessageReaction mr JOIN FETCH mr.message WHERE mr.message.id IN :messageIds")
    List<MessageReaction> findReactionsForMessages(@Param("messageIds") List<Long> messageIds);

    void deleteByMessageIdAndReaction(Long messageId, String reaction);
}