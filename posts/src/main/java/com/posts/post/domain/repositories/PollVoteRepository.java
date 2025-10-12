package com.posts.post.domain.repositories;

import com.posts.post.application.dtos.OptionVoteCount;
import com.posts.post.domain.model.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    @Query("SELECT v.option.id as optionId, COUNT(v) as voteCount " +
            "FROM PollVote v WHERE v.option.id IN :optionIds GROUP BY v.option.id")
    List<OptionVoteCount> countVotesByOptionId(@Param("optionIds") List<Long> optionIds);
    boolean existsByOptionIdAndUserId(Long optionId, String userId);

//    @Query("SELECT v.option.id, COUNT(v) FROM PollVote v WHERE v.option.id IN :optionIds GROUP BY v.option.id")
//    Map<Long, Long> countVotesByOptionIds(@Param("optionIds") List<Long> optionIds);

    boolean existsByOptionPollIdAndUserId(Long pollId, String userId);

    @Query("SELECT v FROM PollVote v WHERE v.option.poll.id = :pollId AND v.userId = :userId")
    List<PollVote> findByPollIdAndUserId(@Param("pollId") Long pollId, @Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM PollVote v WHERE v.option.poll.id = :pollId AND v.userId = :userId")
    void deleteByPollIdAndUserId(@Param("pollId") Long pollId, @Param("userId") String userId);

    @Query("SELECT v.option.id FROM PollVote v WHERE v.option.poll.id = :pollId AND v.userId = :userId")
    List<Long> findVotedOptionIds(@Param("pollId") Long pollId, @Param("userId") String userId);

    @Query("SELECT COUNT(DISTINCT v.userId) FROM PollVote v WHERE v.option.poll.id = :pollId")
    Long countUniqueVoters(@Param("pollId") Long pollId);
}