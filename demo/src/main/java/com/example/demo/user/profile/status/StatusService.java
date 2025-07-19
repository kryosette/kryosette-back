//package com.example.demo.user.profile.status;
//
//import com.example.demo.security.opaque_tokens.TokenService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//
//import java.nio.file.attribute.UserPrincipalNotFoundException;
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.Objects;
//
//@Service
//@RequiredArgsConstructor
//public class StatusService {
//    private final StatusRepository statusRepository;
//    private final RestTemplate restTemplate;
//    private final TokenService tokenService;
//
//    @Transactional
//    public ResponseEntity<?> updateStatus(String token, String userEmail) throws UserPrincipalNotFoundException {
//        String authData = String.valueOf(tokenService.getTokenJsonData(token));
//
//        if (authData.get("userId") == null || authData.get("username") == null) {
//            throw new UserPrincipalNotFoundException("User credentials not found in token");
//        }
//
//        UserStatus status = statusRepository.findById(userEmail)
//                .orElse(new UserStatus(userEmail));
//
//        UserStatus userStatus = new UserStatus();
//        userStatus.setUserEmail(authData.get("username"));
//        userStatus.setOnline(status.isOnline());
//        userStatus.setLastSeen(LocalDateTime.now());
//
//        return ResponseEntity.ok(true);
//    }
//
//
//}
