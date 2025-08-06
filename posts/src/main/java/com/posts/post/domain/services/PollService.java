package com.posts.post.domain.services;

import com.posts.post.domain.requests.PollRequest;
import com.posts.post.domain.responses.PollResponse;
import com.posts.post.domain.responses.VoteRequest;

import java.nio.file.AccessDeniedException;
import java.nio.file.attribute.UserPrincipalNotFoundException;

public interface PollService {
    PollResponse createPoll(Long postId, PollRequest request, String token) throws UserPrincipalNotFoundException;

    PollResponse getPoll(Long postId, String token) throws UserPrincipalNotFoundException;

    PollResponse vote(Long postId, VoteRequest request, String token) throws UserPrincipalNotFoundException;

    void deletePoll(Long postId, String token) throws UserPrincipalNotFoundException, AccessDeniedException;

    void processExpiredPolls();
}