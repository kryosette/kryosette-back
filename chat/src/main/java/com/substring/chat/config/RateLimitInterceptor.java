package com.substring.chat.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Пропускаем если это не метод контроллера
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);

        // Если аннотации нет - пропускаем
        if (rateLimited == null) {
            return true;
        }

        // Используем userId для аутентифицированных пользователей
        String authHeader = request.getHeader("Authorization");
        String userId = "anonymous";

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Здесь можно добавить логику извлечения userId из токена, как у вас в сервисе
            userId = extractUserIdFromToken(authHeader.replace("Bearer ", ""));
        }

        String key = "user_" + userId + "_" + request.getRequestURI();
        int limit = rateLimited.value();

        RateLimitInfo info = requestCounts.computeIfAbsent(key, k -> new RateLimitInfo());

        long currentTime = System.currentTimeMillis();
        if (currentTime > info.getTimeWindow() + 60000) {
            info.reset(currentTime);
        }

        if (info.getCount() >= limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.getWriter().write("Rate limit exceeded. Try again in " +
                                       (60 - ((currentTime - info.getTimeWindow()) / 1000)) + " seconds");
            return false;
        }

        info.increment();
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(limit - info.getCount()));

        return true;
    }

    private String extractUserIdFromToken(String token) {
        // Здесь можно добавить логику извлечения userId из токена
        // Например, если у вас есть сервис для верификации токена
        return "user_" + token.hashCode(); // временная реализация
    }

    private static class RateLimitInfo {
        private int count;
        private long timeWindow;

        public void reset(long time) {
            this.count = 0;
            this.timeWindow = time;
        }

        public void increment() {
            this.count++;
        }

        public int getCount() {
            return count;
        }

        public long getTimeWindow() {
            return timeWindow;
        }
    }
}