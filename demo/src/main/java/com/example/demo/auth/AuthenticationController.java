package com.example.demo.auth;

import com.example.demo.security.opaque_tokens.TokenData;
import com.example.demo.security.opaque_tokens.TokenService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import com.example.demo.auth.password.PasswordValidationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {

    private final TokenService tokenService;
    private final AuthenticationService service;
    private final PasswordValidationService passwordValidationService;
    private final UserRepository userRepository;

    private static final String NODE_SERVER_ADDRESS = "127.0.0.1";
    private static final int NODE_SERVER_PORT = 8081;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.register(request);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest
    ) throws Exception {
        passwordValidationService.validatePassword(request.getPassword());
        AuthenticationResponse response = service.authenticate(request, httpRequest);
        String encryptedPassword = encryptPassword(request.getPassword());

        try (Socket socket = new Socket(NODE_SERVER_ADDRESS, NODE_SERVER_PORT);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            JSONObject json = new JSONObject();
            json.put("password", encryptedPassword);

            String requestToCServer = json.toString();
            out.println(requestToCServer);
//            System.out.println("Sent to C server: " + requestToCServer);

            String responseFromCServer = in.readLine();
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getEmail(),
//                            request.getPassword()
//                    )
//            );
//            System.out.println("Received from C server: " + responseFromCServer);
//            Object principal = authenticate.getPrincipal();
//            if ("OK".equals(responseFromCServer)) {
//                // Generate JWT token if C server authentication is successful
//                User user = (User) principal;
//                UserDetails userDetails = (UserDetails) principal;
//
//                var claims = new HashMap<String, Object>();
//                claims.put("fullName", user.getFullName());
//
//                String jwtToken = jwtService.generateToken(userDetails, user.getId());
//                return ResponseEntity.ok(AuthenticationResponse.builder()
//                        .token(jwtToken)
//                        .build());
//            } else {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body("Authentication failed: " + responseFromCServer);
//            }

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ERROR: Could not communicate with C server");
        }
        return ResponseEntity.ok(response);
    }

    private String encryptPassword(String password) throws Exception {
        String secretKey = "YourSuperSecretKeyWith32Characters!!";

        byte[] keyBytes = normalizeKey(secretKey, 32); // 32 байта = 256 бит

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec ivSpec = new GCMParameterSpec(128, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private byte[] normalizeKey(String key, int requiredSize) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] normalizedKey = new byte[requiredSize];

        System.arraycopy(
                keyBytes,
                0,
                normalizedKey,
                0,
                Math.min(keyBytes.length, requiredSize)
        );

        return normalizedKey;
    }

    @GetMapping("/activate-account")
    public void confirm(
            @RequestParam String token
    ) throws MessagingException {
        service.activateAccount(token);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid authorization header",
                    "message", "Authorization header must start with 'Bearer '"
            ));
        }

        String token = authHeader.substring(7);

        Optional<TokenData> tokenData = tokenService.getTokenData(token);

        if (tokenData.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid token",
                    "message", "Token not found or expired"
            ));
        }

        if (tokenData.get().getExpiresAt().isBefore(java.time.Instant.now())) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Expired token",
                    "message", "Token has expired"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "username", tokenData.get().getUsername(),
                "userId", tokenData.get().getUserId()
        ));
    }

    @PostMapping("/lock-user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void lockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAccountLocked(true);
        userRepository.save(user);
    }

    @PostMapping("/lock-user/{email}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> unlockUser(@PathVariable String email) {
        service.unlockUser(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register-admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> registerAdmin(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.registerAdmin(request);
        return ResponseEntity.accepted().build();
    }
}