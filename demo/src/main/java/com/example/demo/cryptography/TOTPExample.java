package com.example.demo.cryptography;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TOTPExample {

    public static void main(String[] args) {
        String secretKey = "3132333435363738393031323334353637383930";

        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        long timeStep = 30;

        long T = currentTimeSeconds / timeStep;
        String steps = Long.toHexString(T).toUpperCase();
        while (steps.length() < 16) steps = "0" + steps;

        String totpCode = TOTP.generateTOTP(secretKey, steps, "6", "HmacSHA1");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        System.out.println("Текущее время: " + df.format(new Date(currentTimeSeconds * 1000)));
        System.out.println("TOTP код: " + totpCode);
        System.out.println("Код действителен до: " +
                df.format(new Date((currentTimeSeconds / timeStep * timeStep + timeStep) * 1000)));
    }
}