package com.posts.post.post.poll;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long> {
}

interface PollOptionRepository extends JpaRepository<PollOption, Long> {
}