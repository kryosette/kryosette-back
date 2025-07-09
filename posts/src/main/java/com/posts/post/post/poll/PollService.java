package com.posts.post.post.poll;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Survey management service.
 * Provides methods for creating, voting, and obtaining survey results.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository optionRepository;

    /**
     * Creates a new survey.
     *
     * @param question Text of the question.
     * @param options List of response options.
     * @return The created Poll object.
     * @throws IllegalArgumentException if question or options are empty.
     */
    public Poll createPoll(String question, List<String> options) {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("The question cannot be empty");
        }
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one possible answer");
        }
        Poll poll = new Poll();
        poll.setQuestion(question);
        poll.setCreatedAt(LocalDateTime.now());

        List<PollOption> pollOptions = options.stream()
                .map(optionText -> {
                    PollOption option = new PollOption();
                    option.setText(optionText);
                    option.setPoll(poll);
                    return option;
                })
                .collect(Collectors.toList());

        poll.setOptions(pollOptions);
        return pollRepository.save(poll);
    }

    /**
     * Voting for the answer option.
     *
     * @param optionId is the identifier of the response option.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void voteForOption(Long optionId) {
        optionRepository.findById(optionId).ifPresent(option -> {
            option.setVotes(option.getVotes() + 1);
            optionRepository.save(option);
        });
    }

    /**
     * Receives a survey with results by its ID.
     *
     * @param pollId is the ID of the survey.
     * @return the Poll object if the poll is found, otherwise null.
     */
    public Poll getPollWithResults(Long pollId) {
        return pollRepository.findById(pollId).orElse(null);
    }
}