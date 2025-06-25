//package com.example.demo.user;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//import org.webjars.NotFoundException;
//
//import java.nio.file.attribute.UserPrincipalNotFoundException;
//import java.util.Map;
//import java.util.Objects;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//
//    private final RestTemplate restTemplate;
//    private final UserRepository userRepository;
//    private final
//
//    @Transactional
//    public ResponseEntity<?> checkIfLike(
//            String token,
//            Long postId
//    ) throws UserPrincipalNotFoundException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token.trim());
//
//        ResponseEntity<Map> response = restTemplate.exchange(
//                authServiceUrl + "/api/v1/auth/verify",
//                HttpMethod.POST,
//                new HttpEntity<>(headers),
//                Map.class
//        );
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new SecurityException("Ошибка авторизации: " + response.getBody());
//        }
//
//        String userId = (String) Objects.requireNonNull(response.getBody()).get("userId");
//        if (userId == null) {
//            throw new UserPrincipalNotFoundException("User ID не найден в токене");
//        }
//
//        return (ResponseEntity<?>) ResponseEntity.ok();
//    }
//}