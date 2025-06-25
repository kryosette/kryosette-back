package com.example.demo.auth.TWOFA;

import com.example.demo.cryptography.TOTP;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OTPRepository extends JpaRepository<OTP, String> {

}
