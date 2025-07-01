package com.example.demo.user;

import com.example.demo.security.opaque_tokens.TokenData;
import com.example.demo.security.opaque_tokens.TokenService;
import com.example.demo.user.subscritpion.Subscription;
import com.example.demo.user.subscritpion.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.88:3000"}, allowCredentials = "true")
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

//    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final SubscriptionRepository subscriptionRepository;

//    @GetMapping("/me")
//    public ResponseEntity<?> getCurrentUser(
//            Authentication authentication
//    ) {
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        String username = userDetails.getUsername();
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//        UserDto userDto = convertToDto(user);
//
//        return ResponseEntity.ok(userDto);
//    }

//    @GetMapping("/me")
//    public ResponseEntity<UserDto> getCurrentUser(
//            @RequestHeader("Authorization") String authHeader
//    ) {
//        String token = authHeader.substring(7); // Удаляем "Bearer "
//
//        return tokenService.getTokenData(token)
//                .flatMap(tokenData -> userRepository.findById(tokenData.getUserId()))
//                .map(user -> ResponseEntity.ok(mapToDto(user)))
//                .orElse(ResponseEntity.status(401).build());
//    }
//
//    private UserDto mapToDto(User user) {
//        return new UserDto(
//                user.getId(),
//                user.getUsername(),
//                user.getEmail()
//        );
//    }

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
                user.getId(),
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


        UserDto userProfile = new UserDto(
                user.getId(),
                user.getFirstname() + " " + user.getLastname(),
                user.getEmail()
        );

        return ResponseEntity.ok(userProfile);
    }


//    @GetMapping("/usernames")
//    public ResponseEntity<List<String>> getAllUsernames() {
//        List<String> usernames = userRepository.findAllUsernames();
//        return ResponseEntity.ok(usernames);
//    }
//
//    private UserDto convertToDto(User user) {
//        UserDto userDto = new UserDto();
//        userDto.setId(String.valueOf(user.getId()));
//        userDto.setFirstname(user.getFirstname());
//        userDto.setLastname(user.getLastname());
//        userDto.setEmail(user.getEmail());
//        return userDto;
//    }


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

        boolean isSubscribed = subscriptionRepository
                .existsSubscription(currentUserEmail, targetEmail);

        return ResponseEntity.ok(isSubscribed);
    }

    @GetMapping("/followers-count/email/{email}")
    public ResponseEntity<Integer> getFollowersCount(@PathVariable String email) {
        long count = subscriptionRepository.countFollowers(email);
        return ResponseEntity.ok((int) count);
    }
}