package com.posts.post.post.poll;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
public class PollController {
    private final PollService pollService;

    @PostMapping
    public ResponseEntity<Poll> createPoll(@RequestBody PollCreationRequest request) {
        Poll poll = pollService.createPoll(request.getQuestion(), request.getOptions());
        return ResponseEntity.ok(poll);
    }

    @PostMapping("/{pollId}/vote/{optionId}")
    public ResponseEntity<Void> vote(@PathVariable Long pollId, @PathVariable Long optionId) {
        pollService.voteForOption(optionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{pollId}")
    public ResponseEntity<Poll> getPoll(@PathVariable Long pollId) {
        Poll poll = pollService.getPollWithResults(pollId);
        return ResponseEntity.ok(poll);
    }
}