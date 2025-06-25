package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.InetAddress;

@Service
public class BasicFraudDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(BasicFraudDetectionService.class);
    private static final double HIGH_TRANSACTION_THRESHOLD = 1000.0;
    private static final String HIGH_RISK_COUNTRY_CODE = "RU";

    public boolean isFraudulentTransaction(String senderEmail, Integer recipientUserId, Double amount) {
        if (amount > HIGH_TRANSACTION_THRESHOLD) {
            logger.warn("Transaction amount exceeds threshold: {} > {}", amount, HIGH_TRANSACTION_THRESHOLD);
            return true;
        }

        String ipAddress = getClientIP();

        return false;

    }

    private String getClientIP() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("0:0:0:0:0:0:0:1")) {
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ip = inetAddress.getHostAddress();
            }
        }
        return ip;
    }
    public boolean isFraudulentTransfer(String senderEmail, Integer recipientUserId, Double amount) {
        try {
            Thread.sleep(500);
            logger.info("Checking fraudulent transfer for {}", amount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}