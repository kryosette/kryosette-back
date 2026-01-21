package com.example.demo.application.controllers.user;

import com.example.demo.application.dtos.user.UserDto;
import com.example.demo.application.dtos.user.UserDtoPrivate;
import com.example.demo.domain.model.user.subscription.User;
import com.example.demo.domain.repositories.user.UserRepository;
import com.example.demo.domain.services.opaque_tokens.TokenService;
import com.example.demo.domain.requests.user.UpdateUserRequest;
import com.example.demo.domain.model.user.subscription.Subscription;
import com.example.demo.domain.repositories.communication.user.subscription.SubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.88:3000"}, allowCredentials = "true")
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final SubscriptionRepository subscriptionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(
            @RequestHeader("Authorization") String authHeader
    ) {
        String tokenId = authHeader.substring(7);
        return tokenService.getTokenJsonData(tokenId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateUser(
            @RequestBody UpdateUserRequest updateUserRequest,
            Authentication authentication
    ) {

        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        user.setFirstname(updateUserRequest.getFirstName());
        user.setLastname(updateUserRequest.getLastName());

        userRepository.save(user);

        return ResponseEntity.ok("Profile updated successfully!");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }
        String token = authHeader.substring(7);

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();


        UserDto userProfile = new UserDto(
                user.getFirstname() + " " + user.getLastname(),
                user.getEmail()
        );

        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/email/{emailId}")
    public ResponseEntity<?> getUserByEmail(
            @PathVariable String emailId,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }
        String token = authHeader.substring(7);

        Optional<User> userOptional = userRepository.findByEmail(emailId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        UserDtoPrivate userProfile = new UserDtoPrivate(
                user.getFirstname() + " " + user.getLastname(),
                user.getEmail(),
                user.getId()
        );

        return ResponseEntity.ok(userProfile);
    }

    @Transactional
    @PostMapping("/subscribe/email/{targetEmail}")
    public ResponseEntity<?> subscribeToUser(
            @PathVariable String targetEmail,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String currentUserEmail = String.valueOf(tokenService.getTokenJsonData(token));

        if (currentUserEmail.equals(targetEmail)) {
            return ResponseEntity.badRequest().body("Cannot subscribe to yourself");
        }

        if (subscriptionRepository.existsSubscription(currentUserEmail, targetEmail)) {
            return ResponseEntity.badRequest().body("Already subscribed");
        }

        Subscription subscription = new Subscription();
        subscription.setFollowerEmail(currentUserEmail);
        subscription.setFollowingEmail(targetEmail);
        subscriptionRepository.save(subscription);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @DeleteMapping("/unsubscribe/email/{targetEmail}")
    public ResponseEntity<?> unsubscribeFromUser(
            @PathVariable String targetEmail,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String currentUserEmail = String.valueOf(tokenService.getTokenJsonData(token));

        Optional<Subscription> subscription = subscriptionRepository
                .findSubscription(currentUserEmail, targetEmail);

        if (subscription.isEmpty()) {
            return ResponseEntity.badRequest().body("Not subscribed");
        }

        subscriptionRepository.delete(subscription.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/is-subscribed/email/{targetEmail}")
    public ResponseEntity<Boolean> isSubscribedToUser(
            @PathVariable String targetEmail,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String currentUserEmail = String.valueOf(tokenService.getTokenJsonData(token));

        if (subscriptionRepository.existsSubscription(currentUserEmail, targetEmail)) {
            return ResponseEntity.ok(true);
        }

        return ResponseEntity.ok(false);
    }

    @GetMapping("/followers-count/email/{email}")
    public ResponseEntity<Integer> getFollowersCount(@PathVariable String email) {
        long count = subscriptionRepository.countFollowers(email);
        return ResponseEntity.ok((int) count);
    }
}