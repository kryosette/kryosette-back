//package com.example.demo.application.controllers.auth.two_fa.totp;
//
//import com.example.demo.domain.model.auth.two_fa.totp.OTP;
//import com.example.demo.domain.repositories.auth.two_fa.totp.OTPRepository;
//import com.example.demo.domain.services.auth.two_fa.totp.TOTPService;
//import com.example.demo.domain.model.user.subscription.User;
//import com.example.demo.domain.repositories.user.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.apache.kafka.common.errors.ResourceNotFoundException;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/totp")
//@RequiredArgsConstructor
//public class TOTPController {
//
//    private final UserRepository userRepository;
//    private final TOTPService service;
//    private final OTPRepository otpRepository;
//
//    private final String secretKey = "3132333435363738393031323334353637383930";
//
//    @PostMapping("/generate")
//    public ResponseEntity<String> generateTOTP(Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return ResponseEntity.status(401).body("User not authenticated"); // 401 Unauthorized
//        }
//
//        String email = authentication.getName();
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email)); // или throw exception
//
//        if (user == null) {
//            return ResponseEntity.status(404).body("User not found"); // 404 Not Found
//        }
//
//        if (secretKey == null || secretKey.isEmpty()) {
//            return ResponseEntity.badRequest().body("2FA is not enabled for this user."); // 400 Bad Request
//        }
//
//
//        long currentTimeSeconds = System.currentTimeMillis() / 1000;
//        long timeStep = 30;
//        long T = currentTimeSeconds / timeStep;
//        String steps = Long.toHexString(T).toUpperCase();
//        while (steps.length() < 16) steps = "0" + steps;
//
//        String totpCode = TOTP.generateTOTP(secretKey, steps, "6", "HmacSHA1"); // Использование secretKey из БД
//        user.setEnabled2Fa(true);
//        userRepository.save(user);
//
//        OTP otpCode = new OTP(totpCode);
//        otpRepository.save(otpCode);
//        return ResponseEntity.ok(totpCode);
//
//    }
//
//    @GetMapping("/current-code")
//    public ResponseEntity<Map<String, Object>> getCurrentCode() {
//        long currentTimeSeconds = System.currentTimeMillis() / 1000;
//        long timeStep = 30;
//        long T = currentTimeSeconds / timeStep;
//        String steps = Long.toHexString(T).toUpperCase();
//        while (steps.length() < 16) steps = "0" + steps;
//
//        String code = TOTP.generateTOTP(secretKey, steps, "6", "HmacSHA1");
//        int timeRemaining = 30 - (int)(currentTimeSeconds % 30);
//
//        return ResponseEntity.ok(Map.of(
//                "code", code,
//                "timeRemaining", timeRemaining
//        ));
//    }
//
//    @GetMapping("/time-remaining")
//    public int getTimeRemaining() {
//        long currentTimeSeconds = System.currentTimeMillis() / 1000;
//        return 30 - (int)(currentTimeSeconds % 30);
//    }
//}