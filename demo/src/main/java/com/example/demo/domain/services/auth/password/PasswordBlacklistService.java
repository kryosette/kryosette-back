package com.example.demo.domain.services.auth.password;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordBlacklistService {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    private List<String> fetchHashesFromPwned(String prefix) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.pwnedpasswords.com/range/" + prefix;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return Arrays.asList(response.getBody().split("\r\n"));
    }

    public boolean isPasswordBlacklisted(String password) {
        return blacklist.contains(password) || isInPwnedDatabase(password);
    }

    private boolean isInPwnedDatabase(String password) {
        try {
            // 1. Generate SHA-1 hash (not MD5)
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // 2. Convert to hexadecimal string (uppercase)
            String sha1 = bytesToHex(hashBytes).toUpperCase();

            // 3. Split into prefix and suffix
            String prefix = sha1.substring(0, 5);
            String suffix = sha1.substring(5);

            // 4. Check against API
            List<String> hashes = fetchHashesFromPwned(prefix);
            return hashes.stream()
                    .map(h -> h.split(":")[0]) // Extract just the hash part
                    .anyMatch(h -> h.equals(suffix));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }

    // Helper method to convert byte array to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}