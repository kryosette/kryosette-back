package com.example.demo.domain.repositories.communication.friend;

import com.example.demo.domain.requests.communication.friend.FriendRequest;
import com.example.demo.domain.model.user.subscription.User;
import com.example.demo.domain.requests.communication.friend.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, String> {
    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequestStatus status);
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);
}