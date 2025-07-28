package com.example.demo.communication.friend;

import com.example.demo.exceptions.FriendRequestException;
import com.example.demo.security.id_generator.SUUID2;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service layer for managing friend relationships and friend requests.
 * Handles business logic for friend operations with transactional safety.
 */
@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    /**
     * Sends a friend request from sender to receiver with validation checks.
     * @param senderId Email of the user sending request
     * @param receiverId Email of the user receiving request
     * @param tokenId Authentication token for validation
     * @return DTO of created friend request
     * @throws FriendRequestException If validation fails
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FriendRequestDto sendFriendRequest(String senderId, String receiverId, String tokenId) {
        if (senderId.equals(receiverId)) {
            throw new FriendRequestException("Cannot send friend request to yourself");
        }

        User sender = userRepository.findByEmail(senderId)
                .orElseThrow(() -> new FriendRequestException("Sender not found"));
        User receiver = userRepository.findByEmail(receiverId)
                .orElseThrow(() -> new FriendRequestException("Receiver not found"));

        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING)) {
            throw new FriendRequestException("Friend request already sent");
        }

        if (sender.getFriends().contains(receiver)) {
            throw new FriendRequestException("Users are already friends");
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        friendRequest = friendRequestRepository.save(friendRequest);
        return convertToDTO(friendRequest);
    }

    /**
     * Accepts a pending friend request and establishes mutual friendship.
     * @param requestId ID of friend request to accept
     * @param currentUserId ID of user accepting request
     * @return DTO of accepted friend request
     * @throws FriendRequestException If validation fails
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FriendRequestDto acceptFriendRequest(String requestId, String currentUserId) {
        FriendRequest friendRequest = friendRequestRepository.findById(String.valueOf(requestId))
                .orElseThrow(() -> new FriendRequestException("Friend request not found"));

        if (!(Objects.equals(friendRequest.getReceiver().getId(), currentUserId))) {
            throw new FriendRequestException("You are not the receiver of this request");
        }

        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new FriendRequestException("Friend request is not pending");
        }

        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequest = friendRequestRepository.save(friendRequest);

        User sender = friendRequest.getSender();
        User receiver = friendRequest.getReceiver();

        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        String chatLink = SUUID2.generateId();
        userRepository.saveAll(List.of(sender, receiver));

        return convertToDTO(friendRequest);
    }

    /**
     * Rejects a pending friend request.
     * @param requestId ID of friend request to reject
     * @param receiverId ID of user rejecting request
     * @throws FriendRequestException If validation fails
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void rejectFriendRequest(String requestId, String receiverId) {
        FriendRequest friendRequest = friendRequestRepository.findById(String.valueOf(requestId))
                .orElseThrow(() -> new FriendRequestException("Friend request not found"));

        if (!friendRequest.getReceiver().getId().equals(receiverId)) {
            throw new FriendRequestException("You are not the receiver of this request");
        }

        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new FriendRequestException("Friend request is not pending");
        }

        friendRequest.setStatus(FriendRequestStatus.REJECTED);
        friendRequestRepository.save(friendRequest);
    }

    /**
     * Retrieves all pending friend requests for a user.
     * @param userId ID of user to get requests for
     * @return List of pending friend request DTOs
     * @throws FriendRequestException If user not found
     */
    public List<FriendRequestDto> getPendingRequests(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FriendRequestException("User not found"));

        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequestStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts FriendRequest entity to DTO.
     * @param friendRequest Entity to convert
     * @return Converted DTO
     */
    private FriendRequestDto convertToDTO(FriendRequest friendRequest) {
        return FriendRequestDto.builder()
                .id(friendRequest.getId())
                .senderId(friendRequest.getSender().getId())
                .senderUsername(friendRequest.getSender().getUsername())
                .receiverId(friendRequest.getReceiver().getId())
                .receiverUsername(friendRequest.getReceiver().getUsername())
                .status(friendRequest.getStatus().name())
                .createdAt(friendRequest.getCreatedAt())
                .build();
    }

    /**
     * Retrieves friends list for a user.
     * @param userId ID of user to get friends for
     * @return List of friend DTOs
     * @throws EntityNotFoundException If user not found
     */
    public List<FriendDto> getFriends(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return user.getFriends().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts User entity to Friend DTO.
     * @param friend User entity to convert
     * @return Converted Friend DTO
     */
    private FriendDto convertToDto(User friend) {
        return FriendDto.builder()
                .id(friend.getId())
                .username(friend.getUsername())
                .avatarUrl("/avatars/" + friend.getId() + ".jpg")
                .friendsSince(LocalDateTime.now())
                .build();
    }

    /**
     * Removes friendship between two users.
     * @param userId ID of first user
     * @param friendId ID of second user
     * @throws EntityNotFoundException If either user not found
     * @throws IllegalStateException If users aren't friends
     */
    @Transactional
    public void removeFriend(String userId, String friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Friend not found"));

        if (!user.getFriends().contains(friend)) {
            throw new IllegalStateException("Users are not friends");
        }

        user.getFriends().remove(friend);
        friend.getFriends().remove(user);

        userRepository.saveAll(List.of(user, friend));
    }
}