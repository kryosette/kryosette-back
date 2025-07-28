package com.example.demo.auth.key;

public class PublicKeyResponse {
    private final String publicKey;

    public PublicKeyResponse(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }
}