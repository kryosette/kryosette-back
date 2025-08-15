package com.posts.post.domain.services;

import com.posts.post.domain.aspect.GetToken;
import com.posts.post.domain.model.*;
import com.posts.post.domain.repositories.*;
import com.posts.post.domain.requests.PollRequest;
import com.posts.post.domain.responses.PollOptionResponse;
import com.posts.post.domain.responses.PollResponse;
import com.posts.post.domain.responses.VoteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollServiceImpl implements PollService {
    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final PostRepository postRepository;
    private final GetToken getToken;

    ExecutorService executor = Executors.newFixedThreadPool(4);
    CompletionService<PollVote> completionService = new ExecutorCompletionService<>(executor);

    @Override
    @Transactional
    public PollResponse createPoll(Long postId, PollRequest request, String token) {
        String userId = getToken.verifyTokenAndGetUserId(token);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));


        Poll poll = new Poll();
        poll.setPost(post);
        poll.setQuestion(request.getQuestion());
        poll.setMultipleChoice(request.isMultipleChoice());
        poll.setExpiresAt(request.getExpiresAt());

        Poll savedPoll = pollRepository.save(poll);

        List<PollOption> options = request.getOptions().stream()
                .map(optionText -> {
                    PollOption option = new PollOption();
                    option.setPoll(savedPoll);
                    option.setText(optionText);
                    return option;
                })
                .collect(Collectors.toList());

        pollOptionRepository.saveAll(options);
        savedPoll.setOptions(options);

        return mapToPollResponse(savedPoll, userId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PollResponse getPoll(Long postId, String token) throws UserPrincipalNotFoundException {
        String userId = getToken.verifyTokenAndGetUserId(token);

        Poll poll = pollRepository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        boolean voted = pollVoteRepository.existsByOptionPollIdAndUserId(poll.getId(), userId);

        return mapToPollResponse(poll, userId, voted);
    }

    @Override
    @Transactional
    public PollResponse vote(Long postId, VoteRequest request, String token) throws UserPrincipalNotFoundException {
        String userId = getToken.verifyTokenAndGetUserId(token);

        Poll poll = pollRepository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        if (poll.getExpiresAt() != null && poll.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Poll has expired");
        }

        boolean alreadyVoted = pollVoteRepository.existsByOptionPollIdAndUserId(poll.getId(), userId);

        if (!poll.isMultipleChoice() && request.getOptionIds().size() > 1) {
            throw new IllegalArgumentException("This poll does not allow multiple choices");
        }

        if (!alreadyVoted || poll.isMultipleChoice()) {
            // Удаляем предыдущие голоса, если это не множественный выбор
            if (!poll.isMultipleChoice() && alreadyVoted) {
                pollVoteRepository.deleteByPollIdAndUserId(poll.getId(), userId);
            }

            // Добавляем новые голоса
            List<PollOption> options = pollOptionRepository.findAllById(request.getOptionIds());

            if (options.size() != request.getOptionIds().size()) {
                throw new ResourceNotFoundException("One or more options not found");
            }

//            List<Future<PollVote>> futures = options.stream()
//                    .map(option -> completionService.submit(() -> {
//                        PollVote vote = new PollVote();
//                        vote.setOption(option);
//                        vote.setUserId(userId);
//                        return vote;
//                    }))
//                    .toList();

            List<PollVote> votes = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                try {
                    Future<PollVote> future = completionService.take();
                    votes.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Error processing vote", e);
                }
            }

            pollVoteRepository.saveAll(votes);
        }

        return getPoll(postId, token);
    }

    @Override
    @Transactional
    public void deletePoll(Long postId, String token) throws UserPrincipalNotFoundException, AccessDeniedException {
        String userId = getToken.verifyTokenAndGetUserId(token);

        Poll poll = pollRepository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        if (!poll.getPost().getAuthorId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this poll");
        }

        pollRepository.delete(poll);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Каждый час
    public void processExpiredPolls() {
        List<Poll> expiredPolls = pollRepository.findExpiredPolls();
        log.info("Found {} expired polls to process", expiredPolls.size());
    }

    private PollResponse mapToPollResponse(Poll poll, String userId, boolean voted) {
        PollResponse response = new PollResponse();
        response.setId(poll.getId());
        response.setQuestion(poll.getQuestion());
        response.setMultipleChoice(poll.isMultipleChoice());
        response.setExpiresAt(poll.getExpiresAt());
        response.setCreatedAt(poll.getCreatedAt());
        response.setVoted(voted);

        List<Long> optionIds = poll.getOptions().stream()
                .map(PollOption::getId)
                .collect(Collectors.toList());

        Map<Long, Long> voteCounts = optionIds.isEmpty() ?
                Collections.emptyMap() :
                pollOptionRepository.countVotesByOptionIds(optionIds);

        List<PollOptionResponse> optionResponses = poll.getOptions().stream()
                .map(option -> {
                    PollOptionResponse optionResponse = new PollOptionResponse();
                    optionResponse.setId(option.getId());
                    optionResponse.setText(option.getText());
                    optionResponse.setVoteCount(voteCounts.getOrDefault(option.getId(), 0L));

                    if (userId != null) {
                        optionResponse.setVoted(pollVoteRepository.existsByOptionIdAndUserId(option.getId(), userId));
                    }

                    return optionResponse;
                })
                .collect(Collectors.toList());

        response.setOptions(optionResponses);
        response.setTotalVotes(optionResponses.stream().mapToLong(PollOptionResponse::getVoteCount).sum());

        return response;
    }
}