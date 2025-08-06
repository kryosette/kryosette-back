package com.posts.post.domain.repositories;

import com.posts.post.domain.model.Poll;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PollRepository extends JpaRepository<Poll, Long> {

    @EntityGraph(attributePaths = {"options", "options.votes"})
    Optional<Poll> findByPostId(Long postId);

    @Query("SELECT p FROM Poll p WHERE p.expiresAt < CURRENT_TIMESTAMP AND p.expiresAt IS NOT NULL")
    List<Poll> findExpiredPolls();

    @Query("SELECT COUNT(v) FROM PollVote v WHERE v.option.poll.id = :pollId")
    Long countVotesByPollId(@Param("pollId") Long pollId);

    @Query("SELECT COUNT(DISTINCT v.userId) FROM PollVote v WHERE v.option.poll.id = :pollId")
    Long countUniqueVotersByPollId(@Param("pollId") Long pollId);

    boolean existsByPostId(Long postId);

    @Modifying
    @Query("DELETE FROM Poll p WHERE p.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}