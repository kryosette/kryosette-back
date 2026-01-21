package com.example.demo.domain.repositories.auth.two_fa.totp;

import com.example.demo.domain.model.auth.two_fa.totp.OTP;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OTPRepository extends JpaRepository<OTP, String> {

}
