package com.example.demo.auth.key;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/key/generate")
@RequiredArgsConstructor
public class KeyController {

    @GetMapping("/public")
    public PublicKeyResponse getPublicKey() {
        String publicKey = "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----";
        return new PublicKeyResponse(publicKey);
    }
}
