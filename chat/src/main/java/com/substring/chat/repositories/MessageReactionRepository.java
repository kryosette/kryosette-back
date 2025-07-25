package com.substring.chat.repositories;

import com.substring.chat.entities.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {
    List<MessageReaction> findByMessageId(Long messageId);

    @Query("SELECT mr.reaction, COUNT(mr) FROM MessageReaction mr WHERE mr.message.id = :messageId GROUP BY mr.reaction")
    List<Object[]> countReactionsByMessageId(@Param("messageId") Long messageId);

    void deleteByMessageIdAndReaction(Long messageId, String reaction);
}