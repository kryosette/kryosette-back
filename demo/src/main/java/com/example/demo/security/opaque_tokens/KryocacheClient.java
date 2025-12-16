package com.example.demo.security.opaque_tokens;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class KryocacheClient {

    @Value("${kryocache.server.host:0:0:0:0:0:0:0:1}")
    private String host;

    @Value("${kryocache.server.port:6898}")
    private int port;

    @Value("${kryocache.server.timeout:5000}")
    private int timeout;

    @Value("${kryocache.server.max-retries:3}")
    private int maxRetries;

    private final ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        testDnsResolution();
    }

    private void testDnsResolution() {
        String[] hostsToTest = {
                host,
                "::1",
                "0:0:0:0:0:0:0:1",
                "localhost",
                "127.0.0.1"
        };

        for (String testHost : hostsToTest) {
            try {
                InetAddress[] addresses = InetAddress.getAllByName(testHost);

                for (int i = 0; i < addresses.length; i++) {
                    InetAddress addr = addresses[i];
//                    log.info("     Address {}: {}", i + 1, addr.getHostAddress());
//                    log.info("        Class: {}", addr.getClass().getSimpleName());
//                    log.info("        IPv6: {}", addr instanceof Inet6Address);
//                    log.info("        Loopback: {}", addr.isLoopbackAddress());
//                    log.info("        Reachable: {}", addr.isReachable(2000));
                    if (addr.isLoopbackAddress()) {
                        return;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to resolve host {}: {}", testHost, e.getMessage());
            }
        }
    }

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
                try {
                    Socket socket = new Socket();

                    socket.setSoTimeout(timeout);
                    InetAddress[] addresses = InetAddress.getAllByName(host);

                    List<InetAddress> ipv6Addresses = new ArrayList<>();

                    for (InetAddress addr : addresses) {
                        if (addr instanceof Inet6Address) {
                            ipv6Addresses.add(addr);
                        } else {
                            return false;
                        }
                    }

                    for (int i = 0; i < addresses.length; i++) {
                        InetAddress addr = addresses[i];
                        log.info("     [{}] {} (IPv6: {}, Loopback: {})",
                                i, addr.getHostAddress(),
                                addr instanceof Inet6Address,
                                addr.isLoopbackAddress());
                    }

                    InetAddress ipv6Address = null;
                    if (!ipv6Addresses.isEmpty()) {
                        ipv6Address = ipv6Addresses.get(0);
                    } else {
                        throw new UnknownHostException("No IPv6 address found for host: " + host);
                    }

                    InetSocketAddress socketAddress = new InetSocketAddress(ipv6Address, port);

                    socket.connect(socketAddress, timeout);

                    try (OutputStream out = socket.getOutputStream();
                         BufferedReader in = new BufferedReader(
                                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                        byte[] commandBytes = command.getBytes(StandardCharsets.UTF_8);

                        out.write(commandBytes);
                        out.flush();

                        String response = in.readLine();

                        boolean success = response != null && response.contains("OK");

                        socket.close();

                        return success;
                    }

                } catch (SocketTimeoutException e) {
                    log.error("   ⏰ Socket timeout after {} ms: {}", timeout, e.getMessage());
                } catch (ConnectException e) {
                    log.error("   🔌 Connection refused: {}", e.getMessage());
                    log.error("   Possible causes:");
                    log.error("     - Server is not running");
                    log.error("     - Server is not listening on port {}", port);
                    log.error("     - Firewall blocking connection");
                } catch (UnknownHostException e) {
                    log.error("   🏷️ Unknown host: {}", e.getMessage());
                } catch (IOException e) {
                    log.error("   ❌ I/O Error: {}", e.getMessage());
                    log.error("   Error details:", e);
                } catch (Exception e) {
                    log.error("   ⚠️ Unexpected error: {}", e.getMessage());
                    log.error("   Stack trace:", e);
                }

                log.warn("   Attempt {} failed for command '{}'",
                        attempt + 1, command.trim());

                if (attempt < maxRetries - 1) {
                    int backoffMs = 100 * (1 << attempt);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
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
                long startTime = System.currentTimeMillis();

                try {
                    Socket socket = new Socket();
                    socket.setSoTimeout(timeout);

                    InetAddress[] addresses = InetAddress.getAllByName(host);
                    InetAddress ipv6Address = null;

                    for (InetAddress addr : addresses) {
                        if (addr instanceof Inet6Address) {
                            ipv6Address = addr;
                            break;
                        }
                    }

                    if (ipv6Address == null) {
                        throw new UnknownHostException("No IPv6 address found for host: " + host);
                    }

                    InetSocketAddress socketAddress = new InetSocketAddress(ipv6Address, port);
                    socket.connect(socketAddress, timeout);

                    try (OutputStream out = socket.getOutputStream();
                         BufferedReader in = new BufferedReader(
                                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                        byte[] commandBytes = command.getBytes(StandardCharsets.UTF_8);

                        out.write(commandBytes);
                        out.flush();

                        StringBuilder response = new StringBuilder();
                        String line;
                        int lineCount = 0;

                        while ((line = in.readLine()) != null) {
                            response.append(line);
                            lineCount++;
                        }

                        String responseStr = response.toString();

                        socket.close();

                        return responseStr;
                    }

                } catch (SocketTimeoutException e) {
                    log.error("   ⏰ Read timeout: {}", e.getMessage());
                } catch (ConnectException e) {
                    log.error("   🔌 Connection refused: {}", e.getMessage());
                } catch (UnknownHostException e) {
                    log.error("   🏷️ Unknown host: {}", e.getMessage());
                } catch (IOException e) {
                    log.error("   ❌ I/O Error: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("   ⚠️ Unexpected error: {}", e.getMessage());
                }

                if (attempt < maxRetries - 1) {
                    int backoffMs = 100 * (1 << attempt);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            return null;

        } finally {
            lock.unlock();
        }
    }

}