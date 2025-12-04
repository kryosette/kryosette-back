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
                long startTime = System.currentTimeMillis();

                try {
                    Socket socket = new Socket();

                    socket.setSoTimeout(timeout);
                    InetAddress[] addresses = InetAddress.getAllByName(host);

                    List<InetAddress> ipv6Addresses = new ArrayList<>();
                    List<InetAddress> ipv4Addresses = new ArrayList<>();

                    for (InetAddress addr : addresses) {
                        if (addr instanceof Inet6Address) {
                            ipv6Addresses.add(addr);
                        } else {
                            ipv4Addresses.add(addr);
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

                    long connectTime = System.currentTimeMillis() - startTime;

                    try (OutputStream out = socket.getOutputStream();
                         BufferedReader in = new BufferedReader(
                                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                        byte[] commandBytes = command.getBytes(StandardCharsets.UTF_8);

                        out.write(commandBytes);
                        out.flush();

                        String response = in.readLine();
                        long totalTime = System.currentTimeMillis() - startTime;

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

                    long connectTime = System.currentTimeMillis() - startTime;

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

                        long totalTime = System.currentTimeMillis() - startTime;
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

    public boolean ping() {
        long startTime = System.currentTimeMillis();
        String response = sendCommandWithResponse("PING\r\n");
        long elapsedTime = System.currentTimeMillis() - startTime;

        boolean success = response != null && response.contains("PONG");

        if (success) {
            log.info("   ✅ PING SUCCESS in {} ms: {}", elapsedTime, response);
        } else {
            log.error("   ❌ PING FAILED in {} ms", elapsedTime);
            if (response == null) {
                log.error("   No response received");
            } else {
                log.error("   Unexpected response: '{}'", response);
            }
        }

        return success;
    }

    public String testConnection() {
        StringBuilder result = new StringBuilder();
        result.append("=== Connection Test ===\n");

        result.append("\n1. DNS Resolution:\n");
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                result.append(String.format("   - %s (IPv6: %s, Loopback: %s)\n",
                        addr.getHostAddress(),
                        addr instanceof Inet6Address,
                        addr.isLoopbackAddress()));
            }
        } catch (Exception e) {
            result.append("   ❌ Failed: ").append(e.getMessage()).append("\n");
        }

        result.append("\n2. Connection Test:\n");
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(2000);

            InetAddress addr = InetAddress.getByName(host);
            result.append(String.format("   Trying %s:%d...\n", addr.getHostAddress(), port));

            long start = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(addr, port), 2000);
            long elapsed = System.currentTimeMillis() - start;

            result.append(String.format("   ✅ Connected in %d ms\n", elapsed));
            result.append(String.format("   Local: %s:%d\n",
                    socket.getLocalAddress().getHostAddress(), socket.getLocalPort()));
            result.append(String.format("   Remote: %s:%d\n",
                    socket.getInetAddress().getHostAddress(), socket.getPort()));

            result.append("\n3. Command Test:\n");
            try {
                OutputStream out = socket.getOutputStream();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out.write("PING\r\n".getBytes(StandardCharsets.UTF_8));
                out.flush();

                String response = in.readLine();
                result.append("   Command: PING\n");
                result.append("   Response: ").append(response != null ? response : "NULL").append("\n");

            } catch (Exception e) {
                result.append("   ❌ Command failed: ").append(e.getMessage()).append("\n");
            }

        } catch (SocketTimeoutException e) {
            result.append("   ⏰ Timeout after 2000ms\n");
        } catch (ConnectException e) {
            result.append("   🔌 Connection refused\n");
            result.append("   Possible causes:\n");
            result.append("     - Server not running\n");
            result.append("     - Wrong port\n");
            result.append("     - Firewall blocking\n");
        } catch (Exception e) {
            result.append("   ❌ Error: ").append(e.getMessage()).append("\n");
        }

        result.append("\n=== End Test ===\n");

        String testResult = result.toString();
        log.info("Test Results:\n{}", testResult);
        log.info("🔧 === END CONNECTION TEST ===");

        return testResult;
    }

    public String getConfigInfo() {
        return String.format("Host: %s, Port: %d, Timeout: %dms, Retries: %d",
                host, port, timeout, maxRetries);
    }
}