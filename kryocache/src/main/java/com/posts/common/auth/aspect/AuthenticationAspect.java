package com.posts.common.auth.aspect;

import com.posts.common.auth.annotation.RequiresAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * АСПЕКТ = WHAT + WHEN + WHERE
 * WHERE: @RequiresAuth (pointcut)
 * WHEN: @Around (advice type)
 * WHAT: весь код ниже (advice body)
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationAspect {
    private final GetToken getToken;

    @Around("@annotation(com.posts.common.auth.annotation.RequiresAuth)")
    public Object authenticateAndProceed(ProceedingJoinPoint joinPoint, RequiresAuth requiresAuth) throws Throwable {
        String token = extractTokenFromRequest();

        if (token == null || token.isBlank()) {
            throw new SecurityException("Authorization header missing or invalid");
        }

        AuthVerifyResponse authInfo = getToken.verifyToken(token);

        log.info("✅ User authenticated: {}", authInfo.getUsername());

        return joinPoint.proceed();
    }

    public String extractTokenFromRequest() {
        try {
            /*
            RequestContextHolder: This is a special "keeper" (holder). It stores the current request data in a ThreadLocal variable.
            This means that each incoming user (thread) will have its own data, and it won't be mixed up.
            .getRequestAttributes(): This method retrieves the current request data. But there's a catch: it returns an object of the RequestAttributes shared interface.
            (ServletRequestAttributes): This is a type cast. We're forcing Java to tell it, "I know for sure that this shared object contains attributes for the Servlet (HTTP), and nothing else."
            ServletRequestAttributes attributes: This creates a variable in which we store the result.
             */
           ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                log.warn("No HTTP request context");
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7).trim();
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting token", e);
            return null;
        }
    }

    public String verifyTokenAndGetUserId(String token) {
        return verifyToken(token).getUserId();
    }

    public String verifyTokenAndGetEmail(String token) {
        return verifyToken(token).getUsername();
    }
}
