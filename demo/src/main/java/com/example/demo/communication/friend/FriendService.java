package com.example.demo.communication.friend;

import com.example.demo.communication.chat.room.RoomRepository;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

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

//        Room chat = new Room();
//        chat.setChatLink(chatLink);
//        chat.setParticipants(List.of(sender, receiver));
//        chatRepository.save(chat);
        userRepository.saveAll(List.of(sender, receiver));
  
        return convertToDTO(friendRequest);
    }

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

    public List<FriendRequestDto> getPendingRequests(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FriendRequestException("User not found"));

        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequestStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

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

    public List<FriendDto> getFriends(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return user.getFriends().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private FriendDto convertToDto(User friend) {
        return FriendDto.builder()
                .id(friend.getId())
                .username(friend.getUsername())
                .avatarUrl("/avatars/" + friend.getId() + ".jpg") // или friend.getAvatarUrl() если есть в User
                .friendsSince(LocalDateTime.now()) // или реальная дата из связи если есть
                .build();
    }

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