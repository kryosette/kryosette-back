package com.example.demo.auth;

import com.example.demo.auth.email.EmailService;
import com.example.demo.auth.email.EmailTemplateName;
import com.example.demo.security.opaque_tokens.TokenService;
import com.example.demo.user.*;
import com.example.demo.user.role.RoleRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Core authentication service handling user registration, authentication,
 * account activation, and admin operations.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>User registration with email verification</li>
 *   <li>Credential authentication</li>
 *   <li>Opaque token generation</li>
 *   <li>Account activation/locking</li>
 *   <li>Admin-specific workflows</li>
 * </ul>
 *
 * @see TokenService For opaque token management
 * @see EmailService For email delivery
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final CacheManager cacheManager;

    /**
     * Frontend activation URL for account verification emails.
     * Injected from Spring properties.
     */
    @Value("${spring.mailing.frontend.activation-url}")
    private String activationUrl;

    /**
     * Registers a new user account with default USER role.
     *
     * @param request Validated registration data
     * @throws MessagingException If activation email fails to send
     * @throws IllegalStateException If USER role is not configured in database
     * @implNote Flow:
     * <ol>
     *   <li>Creates disabled account</li>
     *   <li>Stores BCrypt-hashed password</li>
     *   <li>Sends activation email</li>
     * </ol>
     */
    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .enabled2Fa(false)
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    /**
     * Authenticates user credentials and generates an opaque token.
     *
     * @param request Authentication request (email + password)
     * @param httpRequest For device fingerprint generation
     * @return AuthenticationResponse containing opaque token
     * @throws LockedException If account is administratively locked
     * @throws IllegalStateException If principal type mismatch occurs
     * @implNote Security flow:
     * <ol>
     *   <li>Validates credentials via Spring Security</li>
     *   <li>Checks account lock status</li>
     *   <li>Generates device-specific token</li>
     * </ol>
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpRequest) {
        log.info("=== AUTH SERVICE START ===");
        log.info("Service - Email: {}", request.getEmail());

        try {
            log.info("1. Checking auth attempts cache");
            String cacheKey = "auth_attempt:" + request.getEmail();
            if (Objects.requireNonNull(cacheManager.getCache("authAttempts")).get(cacheKey) != null) {
                log.warn("2. Too many attempts for: {}", request.getEmail());
                throw new LockedException("Too many attempts");
            }
            log.info("2. Auth attempts check passed");

            log.info("3. Starting AuthenticationManager.authenticate()");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            log.info("4. AuthenticationManager.authenticate() successful");

            User user = (User) authentication.getPrincipal();
            log.info("5. User found: {}", user.getEmail());
            log.info("6. User enabled: {}, locked: {}", user.isEnabled(), user.isAccountLocked());

            if (user.isAccountLocked()) {
                log.warn("7. Account is locked: {}", user.getEmail());
                Objects.requireNonNull(cacheManager.getCache("lockedAccounts")).put(user.getEmail(), true);
                throw new LockedException("Account is locked");
            }
            log.info("7. Account lock check passed");

            log.info("8. Generating device hash");
            String deviceHash = generateDeviceHash(httpRequest, user.getUsername());
            String clientIp = getClientIpAddress(httpRequest);
            log.info("9. Device hash: {}, IP: {}", deviceHash, clientIp);

            log.info("10. Generating token");
            String token = tokenService.generateToken(user, user.getId(), deviceHash, clientIp);
            log.info("11. Token generated successfully");

            log.info("=== AUTH SERVICE SUCCESS ===");
            return AuthenticationResponse.builder()
                    .token(token)
                    .build();

        } catch (BadCredentialsException e) {
            log.error("=== AUTH SERVICE - BAD CREDENTIALS ===");
            log.error("Bad credentials for: {}", request.getEmail());
            throw e;
        } catch (LockedException e) {
            log.error("=== AUTH SERVICE - LOCKED ===");
            log.error("Locked exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== AUTH SERVICE - UNEXPECTED ERROR ===");
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extracts client IP address from HttpServletRequest considering proxies and load balancers.
     *
     * @param request HTTP request
     * @return Client IP address or "unknown" if cannot determine
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Порядок проверки заголовков важен!
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (isValidIpAddress(ip)) {
                // X-Forwarded-For может содержать список IP: "client, proxy1, proxy2"
                if (header.equals("X-Forwarded-For") && ip.contains(",")) {
                    String[] ips = ip.split(",");
                    ip = ips[0].trim(); // берем первый IP (клиент)
                }
                return ip;
            }
        }

        // Если заголовки не помогли, берем remote address
        return request.getRemoteAddr();
    }

    /**
     * Validates IP address format.
     *
     * @param ip IP address to validate
     * @return true if valid IPv4 or IPv6 address
     */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        try {
            // Проверяем IPv4 и IPv6
            InetAddress inetAddress = InetAddress.getByName(ip);
            return !inetAddress.isAnyLocalAddress() &&
                    !inetAddress.isLoopbackAddress() &&
                    !inetAddress.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Generates a device fingerprint hash for session binding.
     *
     * @param request HTTP request for headers
     * @param username For binding to user identity
     * @return SHA-256 hash of device characteristics
     * @implNote Fingerprint components:
     * <ul>
     *   <li>User-Agent header</li>
     *   <li>Accept-Language header</li>
     *   <li>Sec-CH-UA-Platform header</li>
     *   <li>Username</li>
     *   <li>Day granularity timestamp</li>
     * </ul>
     */
    public String generateDeviceHash(HttpServletRequest request, String username) {
        String deviceFingerprint = String.join("|",
                request.getHeader("User-Agent"),
                request.getHeader("Accept-Language"),
                request.getHeader("Sec-CH-UA-Platform"),
                username,
                String.valueOf(System.currentTimeMillis() / (1000 * 60 * 60 * 24))
        );
        return DigestUtils.sha256Hex(deviceFingerprint);
    }

    /**
     * Activates user account using verification token.
     *
     * @param token Activation token (6-digit code)
     * @throws MessagingException If token expired and resend fails
     * @throws RuntimeException For invalid/expired tokens
     * @implNote Transactional flow:
     * <ol>
     *   <li>Validates token existence</li>
     *   <li>Checks expiration</li>
     *   <li>Activates user account</li>
     *   <li>Marks token as used</li>
     * </ol>
     */
    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been send to the same email address");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    /**
     * Generates and persists an activation token.
     *
     * @param user User to associate with token
     * @return Generated 6-digit token
     */
    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generatedToken;
    }

    /**
     * Sends account activation email.
     *
     * @param user Target user with email address
     * @throws MessagingException If email delivery fails
     */
    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }

    /**
     * Generates a numeric activation code.
     *
     * @param length Code length (typically 6)
     * @return Cryptographically secure random number string
     */
    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    /**
     * Locks user account (admin operation).
     *
     * @param email User email to lock
     * @throws UsernameNotFoundException If user doesn't exist
     */
    public void lockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAccountLocked(true);
        userRepository.save(user);
    }

    /**
     * Unlocks user account (admin operation).
     *
     * @param email User email to unlock
     * @throws UsernameNotFoundException If user doesn't exist
     */
    public void unlockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAccountLocked(false);
        userRepository.save(user);
    }

    /**
     * Registers admin user with elevated privileges.
     *
     * @param request Admin registration data
     * @throws MessagingException If email delivery fails
     * @throws IllegalStateException If ADMIN role is not configured
     * @implNote Differences from regular registration:
     * <ul>
     *   <li>Immediate activation (enabled=true)</li>
     *   <li>ADMIN role assignment</li>
     * </ul>
     */
    public void registerAdmin(RegistrationRequest request) throws MessagingException {
        var adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE ADMIN was not initiated"));

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(true)
                .roles(List.of(adminRole))
                .build();

        userRepository.save(user);
    }
}