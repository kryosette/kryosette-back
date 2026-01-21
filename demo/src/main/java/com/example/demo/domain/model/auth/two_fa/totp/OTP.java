package com.example.demo.domain.model.auth.two_fa.totp;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OTP {
    @Id
    private String code;

    public OTP() {}

    public OTP(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
