package com.posts.post.domain.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("like")
@Slf4j
public class LikeControllerKryocache {
    private final LikeService likeService;
    private final TokenService tokenService;

    @PostMapping("/toggle")
    public ResponseEntity<?> createLike(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer postId
    ) {
        try {
            String formattedToken;
            if (token != null && token.startsWith("Bearer ")) {
                formattedToken = token.substring(7);
            } else {
                throw new IllegalArgumentException("Invalid or missing Bearer token");
            }

            String userId = tokenService.verifyTokenAndGet(formattedToken);

            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "invalid token or doesn't exist token"));
            }

            Map<String, Object> result = tokenService.toggleLike(postId, Long.valueOf(postId));

            if (result.containsKey("error")) {
                return ResponseEntity.badRequest().body(Map.of("error", "bad request (tokenService.toggleLike)"));
            }

            return ResponseEntity.ok(Map.of(
              "liked", result.get("liked"),
              "count", result.get("count")
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError(
                    Map.of("error", "internal server error");
            );
        }
    }
}
