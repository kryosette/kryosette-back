package com.example.demo.security.opaque_tokens;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class KryocacheClient {

    @Value("${kryocache.server.host:::1}")
    private String host;

    @Value("${kryocache.server.port:6898}")
    private int port;

    @Value("${kryocache.server.timeout:5000}")
    private int timeout;

    @Value("${kryocache.server.max-retries:3}")
    private int maxRetries;

    private final ReentrantLock lock = new ReentrantLock();

    public boolean set(String key, String value) {
        return executeCommand("SET " + key + " " + value + "\r\n");
    }

    public String get(String key) {
        String command = "GET " + key + "\r\n";
        return sendCommandWithResponse(command);
    }

    public boolean exists(String key) {
        String response = sendCommandWithResponse("EXISTS " + key + "\r\n");
        return "true".equalsIgnoreCase(response) || "1".equals(response);
    }

    public boolean delete(String key) {
        return executeCommand("DELETE " + key + "\r\n");
    }

    public boolean setToken(String token, String data, int ttlSeconds) {
        String value = data + "|TTL:" + ttlSeconds;
        return set(token, value);
    }

    public String getToken(String token) {
        String data = get(token);
        if (data == null || data.isEmpty()) {
            return null;
        }

        // Убираем TTL информацию если она есть
        int ttlIndex = data.indexOf("|TTL:");
        if (ttlIndex > 0) {
            return data.substring(0, ttlIndex);
        }
        return data;
    }

    public boolean validateToken(String token) {
        return exists(token);
    }

    private boolean executeCommand(String command) {
        lock.lock();
        try {
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try (Socket socket = new Socket();
                     OutputStream out = socket.getOutputStream();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                    socket.setSoTimeout(timeout);
                    socket.connect(new InetSocketAddress(host, port), timeout);

                    out.write(command.getBytes(StandardCharsets.UTF_8));
                    out.flush();

                    String response = in.readLine();
                    log.debug("Command: {}, Response: {}", command.trim(), response);

                    return response != null && response.contains("OK");

                } catch (Exception e) {
                    log.warn("Attempt {} failed for command {}: {}",
                            attempt + 1, command.trim(), e.getMessage());

                    if (attempt < maxRetries - 1) {
                        try {
                            Thread.sleep(100 * (1 << attempt)); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private String sendCommandWithResponse(String command) {
        lock.lock();
        try {
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try (Socket socket = new Socket();
                     OutputStream out = socket.getOutputStream();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                     socket.setSoTimeout(timeout);
                     socket.connect(new InetSocketAddress(host, port), timeout);

                    out.write(command.getBytes(StandardCharsets.UTF_8));
                    out.flush();

                    StringBuilder response = new StringBuilder();
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }

                    return response.toString();

                } catch (Exception e) {
                    log.warn("Attempt {} failed for command {}: {}",
                            attempt + 1, command.trim(), e.getMessage());

                    if (attempt < maxRetries - 1) {
                        try {
                            Thread.sleep(100 * (1 << attempt));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public boolean ping() {
        String response = sendCommandWithResponse("PING\r\n");
        return response != null && response.contains("PONG");
    }
}
