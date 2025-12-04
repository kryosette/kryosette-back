package com.example.demo.security.opaque_tokens;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenServiceDev tokenService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createToken(
            @RequestBody TokenRequest request) {

        String token = tokenService.createToken(
                request.getUserId(),
                request.getUserData(),
                request.getTtlSeconds()
        );

        if (token == null) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create token"));
        }

        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<Map<String, Boolean>> validateToken(
            @PathVariable String token) {

        boolean isValid = tokenService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getTokenData(@PathVariable String token) {
        TokenDataDev data = tokenService.getTokenData(token);

        if (data == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Map<String, Boolean>> revokeToken(
            @PathVariable String token) {

        boolean revoked = tokenService.revokeToken(token);
        return ResponseEntity.ok(Map.of("revoked", revoked));
    }

    @PutMapping("/refresh/{token}")
    public ResponseEntity<Map<String, Boolean>> refreshToken(
            @PathVariable String token,
            @RequestParam(defaultValue = "3600") int ttlSeconds) {

        boolean refreshed = tokenService.refreshToken(token, ttlSeconds);
        return ResponseEntity.ok(Map.of("refreshed", refreshed));
    }

    @lombok.Data
    public static class TokenRequest {
        private String userId;
        private String userData;
        private int ttlSeconds = 3600; // 1 час по умолчанию
    }
}