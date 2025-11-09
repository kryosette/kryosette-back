//package com.substring.chat.config;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import org.springframework.web.context.request.WebRequestInterceptor;
//
//import java.util.Map;
//
//@FeignClient(name = "auth-service", url = "${auth.service.url}")
//public interface AuthServiceClient {
//
//    @PostMapping("/api/v1/auth/verify")
//    ResponseEntity<Map<String, Object>> verifyToken(
//            @RequestHeader("Authorization") String token
//    );
//}