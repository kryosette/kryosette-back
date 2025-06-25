package com.example.demo.auth.password;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Service
@RequiredArgsConstructor
public class PasswordValidationService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordBlacklistService blacklistService;

    public void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw Problem.valueOf(Status.BAD_REQUEST, "Password cannot be empty");
        }

        if (rawPassword.length() < 12) {
            throw Problem.valueOf(Status.BAD_REQUEST, "Password too short");
        }

        if (!containsRequiredChars(rawPassword)) {
            throw Problem.valueOf(Status.BAD_REQUEST,
                    "Password must include: uppercase, lowercase, digit, special char");
        }

        if (blacklistService.isPasswordBlacklisted(rawPassword)) {
            throw Problem.valueOf(Status.BAD_REQUEST, "This password is compromised");
        }

        if (hasRepeatingSequences(rawPassword, 3)) {
            throw Problem.valueOf(Status.BAD_REQUEST, "Password contains repeating sequences");
        }
    }

    private boolean containsRequiredChars(String password) {
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = !password.matches("[A-Za-z0-9]*");
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private boolean hasRepeatingSequences(String password, int maxAllowed) {
        for (int i = 0; i < password.length() - maxAllowed; i++) {
            String sequence = password.substring(i, i + maxAllowed);
            if (password.indexOf(sequence, i + 1) != -1) {
                return true;
            }
        }
        return false;
    }
}