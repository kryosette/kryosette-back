package com.substring.chat.infastructure.config;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class StatusController {

    private final SimpMessagingTemplate messagingTemplate;

    public StatusController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/status.update")
    public void handleStatusUpdate(StatusMessage message, Principal principal) {
        String userId = principal.getName(); // Или из message
        messagingTemplate.convertAndSend(
                "/topic/status/" + userId,
                Map.of("online", message.isOnline())
        );
    }
}

