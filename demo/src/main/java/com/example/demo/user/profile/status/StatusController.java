package com.example.demo.user.profile.status;

import com.example.demo.security.opaque_tokens.TokenService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.handler.annotation.Header;

@Controller
@RequiredArgsConstructor
public class StatusController {

    private final SimpMessagingTemplate messagingTemplate;
    private final StatusRepository statusRepository;

    @MessageMapping("/status.update")
    public void updateStatus(Principal principal, StatusMessage message) {
        String userId = principal.getName();

        UserStatus status = statusRepository.findById(userId)
                .orElse(new UserStatus(userId));

        status.setOnline(message.isOnline());
        status.setLastSeen(LocalDateTime.now());
        statusRepository.save(status);

        messagingTemplate.convertAndSend(
                "/topic/status/" + userId,
                Map.of(
                        "online", status.isOnline(),
                        "lastSeen", status.getLastSeen()
                )
        );
    }

    @GetMapping("/api/status/{userId}")
    public ResponseEntity<UserStatus> getStatus(@PathVariable String userId) {
        return ResponseEntity.ok(
                statusRepository.findById(userId)
                        .orElse(new UserStatus(userId))
        );
    }
}