package com.example.demo.infastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3000",
            "http://192.168.1.88:3000",
            "http://192.168.1.92:3000",
            "http://localhost:8080",
            "http://localhost:8091",
            "http://10.193.204.156:3000"
    };

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(ALLOWED_ORIGINS)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
                registry.addMapping("/**")
                        .allowedOrigins(ALLOWED_ORIGINS)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);

                registry.addMapping("/api/v1/tokens/**")
                        .allowedOrigins(ALLOWED_ORIGINS)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);

                registry.addMapping("/api/v1/auth/**")
                        .allowedOrigins(ALLOWED_ORIGINS)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);

                registry.addMapping("/api/v1/posts/**")
                        .allowedOrigins(ALLOWED_ORIGINS)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);

                registry.addMapping("/api/v1/user/me")
                        .allowedOrigins(ALLOWED_ORIGINS)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);

                registry.addMapping("/ws/**")
                        .allowedOrigins("http://localhost:3000", "http://192.168.1.88:3000")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
                registry.addMapping("/api/v1/auth/lock-user-by-device")
                        .allowedOrigins("*")
                        .allowedMethods("POST", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);
                registry.addMapping("/api/v1/auth/token/by-device/{deviceHash}")
                        .allowedOrigins("*")
                        .allowedMethods("POST", "OPTIONS", "GET")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);


            }
        };
    }
}