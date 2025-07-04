package com.posts.post.post.upload_files;


import lombok.Getter;

import java.security.SecureRandom;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public final class SUUID2 {
    private static final int DEFAULT_UUID_BYTE_LENGTH = 16;
    private static final int DEFAULT_ENTROPY_REFRESH_INTERVAL = 100;
    private static final Set<String> USED_UUIDS = Collections.newSetFromMap(new ConcurrentHashMap<>(1_000_000));

    private static final SecureRandom SECURE_RANDOM;
    private static final AtomicInteger generationCounter = new AtomicInteger(0);
    @Getter
    private static int uuidByteLength = DEFAULT_UUID_BYTE_LENGTH;
    private static int uuidEntropyLength = DEFAULT_ENTROPY_REFRESH_INTERVAL;

    static {
        try {
            SECURE_RANDOM = SecureRandom.getInstanceStrong();
            SECURE_RANDOM.nextBytes(new byte[1]);

            if (SECURE_RANDOM.getAlgorithm().equals("NativePRNGNonBlocking") &&
                    !System.getProperty("os.name").toLowerCase().contains("linux")) {
                throw new SecurityException("Insecure RNG configuration");
            }
        } catch (Exception e) {
            throw new SecurityException("Failed to initialize SecureRandom", e);
        }
    }

    // Новый метод для установки длины UUID
    public static void setUuidByteLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Длина UUID должна быть положительной.");
        }
        uuidByteLength = length;
    }

    public static void setUuidEntropyLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Длина ");
        }
        uuidEntropyLength = length;
    }

    public static String generateId() {
        if (generationCounter.incrementAndGet() % uuidEntropyLength == 0) {
            refreshEntropy();
        }

        String uuid;
        int attempts = 0;
        do {
            if (attempts > 10) {
                throw new SecurityException("Too many UUID collisions detected.");
            }
            byte[] bytes = new byte[uuidByteLength];

            synchronized (SECURE_RANDOM) {
                SECURE_RANDOM.nextBytes(bytes);
            }

            uuid = bytesToHex(bytes);

            if (USED_UUIDS.size() > 1_000_000) {
                USED_UUIDS.clear();
                throw new SecurityException("UUID collision threshold exceeded");
            }

        } while (!USED_UUIDS.add(uuid));

        return uuid;
    }

    private static void refreshEntropy() {
        synchronized (SECURE_RANDOM) {
            SECURE_RANDOM.setSeed(SECURE_RANDOM.generateSeed(uuidByteLength)); // Используем переменную uuidByteLength
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static void main(String[] args) {
        // Пример использования
        System.out.println("16 byte: " + generateId());
        SUUID2.setUuidByteLength(20);
        System.out.println("20 byte: " + generateId());
        SUUID2.setUuidByteLength(24);
        System.out.println("24 byte: " + generateId());
        SUUID2.setUuidByteLength(28);
        System.out.println("28 byte: " + generateId());
        SUUID2.setUuidByteLength(32); // Устанавливаем длину 32 байта
        System.out.println("32 byte: " + generateId());

        System.out.println("Текущая длина UUID: " + SUUID2.getUuidByteLength());
    }

    private SUUID2() {
        throw new AssertionError("Cannot instantiate utility class");
    }
}
