package com.posts.post.domain.repositories;

import com.posts.post.domain.model.Poll;
import com.posts.post.domain.model.PollOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    @EntityGraph(attributePaths = "votes")
    List<PollOption> findByPollId(Long pollId);

    @Query("SELECT po FROM PollOption po WHERE po.poll.id = :pollId ORDER BY po.id")
    List<PollOption> findOptionsByPollId(@Param("pollId") Long pollId);

    @Query("SELECT v.option.id, COUNT(v) FROM PollVote v WHERE v.option.poll.id IN :pollIds GROUP BY v.option.id")
    Map<Long, Long> countVotesByOptionIds(@Param("pollIds") List<Long> pollIds);

    @Query("SELECT COUNT(v) FROM PollVote v WHERE v.option.id = :optionId")
    Long countVotesByOptionId(@Param("optionId") Long optionId);

    @EntityGraph(attributePaths = {"poll"})
    List<PollOption> findByPollIdIn(List<Long> pollIds);

    boolean existsByIdAndPollId(Long optionId, Long pollId);
}