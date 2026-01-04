package com.example.demo.security.opaque_tokens;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceHashTest implements CommandLineRunner {

    private final TokenService tokenService;

    @Override
    public void run(String... args) throws Exception {
        String deviceHash = "43d7b9bc055ba8687832018a4528165b4a294e4d2ec10d25a60d31d5fa327df5";

        System.out.println("=== Тестирование поиска токена по device hash ===");
        System.out.println("Device hash: " + deviceHash);

        String tokenData = tokenService.findTokenByDeviceHash(deviceHash);

        if (tokenData == null) {
            System.out.println("❌ Токен не найден!");
        } else {
            System.out.println("✅ Токен найден:");
            System.out.println(tokenData);
        }

        // Можно протестировать несколько device hash
        testAllDeviceHashes();
    }

    private void testAllDeviceHashes() {
        System.out.println("\n=== Тестирование всех device hashes ===");

        // Здесь можно добавить логику для получения всех device hashes из базы
        // Или тестировать конкретные значения
    }
}
