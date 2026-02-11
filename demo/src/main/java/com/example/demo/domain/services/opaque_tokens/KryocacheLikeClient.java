//package com.example.demo.domain.services.opaque_tokens;
//
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.task.SyncTaskExecutor;
//import org.springframework.stereotype.Component;
//
//import java.io.*;
//import java.net.*;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.concurrent.atomic.LongAdder;
//import java.util.concurrent.locks.ReentrantLock;
//
//@Slf4j
//public class KryocacheLikeClient {
//    private static class SyncJob {
//        final String key;
//        final boolean newStatus;
//        final long timestamp;
//        int retryCount = 0;
//
//        SyncJob(String key, boolean newStatus) {
//            this.key = key;
//            this.newStatus = newStatus;
//            this.timestamp = System.currentTimeMillis();
//        }
//
//        String getCommand() {
//            return newStatus ?
//                    "SET " + key + " true EX 2592000\r\n" :
//                    "DELETE " + key + "\r\n";
//        }
//    }
//
//    @Value("${kryocache.server.host:0:0:0:0:0:0:0:1}")
//    private String host;
//
//    @Value("${kryocache.server.port:6898}")
//    private int port;
//
//    @Value("${kryocache.server.timeout:100}") // Уменьшаем timeout для лайков
//    private int timeout;
//
//    @Value("${kryocache.server.max-retries:2}")
//    private int maxRetries;
//
//    private static final int LOCK_POOL_SIZE = 16; // Оптимально для большинства случаев
//    private final Map<String, Object> keyLocks = new ConcurrentHashMap<>();
//
//    private final BlockingQueue<SyncJob> retryQueue = new LinkedBlockingQueue<>();
//
//    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(
//            Runtime.getRuntime().availableProcessors(),
//            new ThreadFactory() {
//                private final AtomicInteger counter = new AtomicInteger();
//                @Override
//                public Thread newThread(Runnable r) {
//                    Thread thread = new Thread(r, "like-sync-" + counter.incrementAndGet());
//                    thread.setDaemon(true);
//                    return thread;
//                }
//            }
//    );
//
//    private final ReentrantLock lock = new ReentrantLock();
//
//    /*
//┌─────────────────────────────────────────────────────┐
//│                   ЯДРО 0                            │
//│  ┌──────────────────────────────────────────────┐   │
//│  │  Регистры (1 цикл)                           │   │
//│  │  ┌──────────────────────────────────────┐    │   │
//│  │  │   L1 Кэш (1-3 цикла)                 │    │   │
//│  │  │  ┌──────────┐  ┌──────────┐          │    │   │
//│  │  │  │ L1d Data │  │ L1i Inst │          │    │   │
//│  │  │  │  32-64KB │  │  32-64KB │          │    │   │
//│  │  │  └──────────┘  └──────────┘          │    │   │
//│  │  └──────────────────────────────────────┘    │   │
//│  │                 ↓ (12-20 циклов)             │   │
//│  │  ┌──────────────────────────────────────┐    │   │
//│  │  │   L2 Кэш (256KB - 1MB)               │    │   │
//│  │  │  Unified (данные + инструкции)       │    │   │
//│  │  └──────────────────────────────────────┘    │   │
//│  └──────────────────────────────────────────────┘   │
//│                     ↓ (30-60 циклов)                │
//│  ┌────────────────────────────────────────────────┐ │
//│  │           L3 Кэш (Shared, 8-128MB)             │ │
//│  │  Общий для всех ядер, используется как         │ │
//│  │  когерентный буфер (LLC - Last Level Cache)    │ │
//│  └────────────────────────────────────────────────┘ │
//│                     ↓ (200-400 циклов)              │
//│                   ОЗУ (RAM)                         │
//└─────────────────────────────────────────────────────┘
//     */
//    private final Map<String, LongAdder> localCounters = new ConcurrentHashMap<>();
//    /*
//    check if like ?
//     */
//    private final Map<String, Boolean> localLikeStatus = new ConcurrentHashMap<>();
//
//    /*
//    Statistics
//     */
//    private final AtomicLong totalOperations = new AtomicLong(0);
//    private final AtomicLong failedOperations = new AtomicLong(0);
//    private final LongAdder currentLoad = new LongAdder();
//
//    /*
//    @PostConstruct is an annotation used to mark a method that should be executed immediately after the container
//    (e.g. Spring or Java EE/Jakarta EE) creates an instance of a class and completes all dependency injection.
//     */
//    @PostConstruct
//    public void init() {
//        log.info("KryocacheLikeClient initialized for likes system");
//        // Запускаем периодическую синхронизацию локальных счетчиков
//        startSyncScheduler();
//    }
//
//    public long incrementLikeCount(String postId) {
//        String key = "like:count:" + postId;
//
//        LongAdder localCounter = localCounters.computeIfAbsent(key, k -> new LongAdder());
//        localCounter.increment();
//
//        asyncSyncToKryocache(key, "INCR");
//
//        return localCounter.sum();
//    }
//
//    public long decrementLikeCount(String postId) {
//        String key = "like:count:" + postId;
//
//        LongAdder localCounter = localCounters.computeIfAbsent(key, k -> new LongAdder());
//        localCounter.decrement();
//
//        asyncSyncToKryocache(key, "DECR");
//
//        return localCounter.sum();
//    }
//
//    public boolean toggleUserLike(String postId, String userId) {
//        String key = "like:user:" + postId + ":" + userId;
//
//        Boolean cached = localLikeStatus.get(key);
//        if (cached != null) {
//            return toggleExistingStatus(key, cached);
//        }
//
//        return toggleWithRaceConditionProtection(key);
//    }
//
//    private boolean toggleExistingStatus(String key, boolean currentStatus) {
//        boolean newStatus = !currentStatus;
//
//        localLikeStatus.put(key, newStatus);
//
//        sendSyncCommandAsync(key, newStatus);
//
//        return newStatus;
//    }
//
//    private boolean toggleWithRaceConditionProtection(String key) {
//        synchronized (getLockForKey(key)) {
//            Boolean cached = localLikeStatus.get(key);
//            if (cached != null) {
//                return toggleExistingStatus(key, cached);
//            }
//
//            String result = sendCommandWithResponse("GET " + key + "\r\n");
//            boolean currentStatus = isTrue(result);
//            boolean newStatus = !currentStatus;
//
//            localLikeStatus.put(key, newStatus);
//            sendSyncCommandAsync(key, newStatus);
//
//            return newStatus;
//        }
//    }
//
//    private Object getLockForKey(String key) {
//        return keyLocks.computeIfAbsent(
//                key.hashCode() % LOCK_POOL_SIZE,
//                k -> new Object()
//        );
//    }
//
//    private void sendSyncCommandAsync(String key, boolean newStatus) {
//        CompletableFuture.runAsync(() -> {
//            String command = newStatus ?
//                    "SET " + key + " true EX 2592000\r\n" :
//                    "DELETE " + key + "\r\n";
//
//            try {
//                asyncSyncToKryocache(key, command);
//            } catch (Exception e) {
//                log.error("Failed to sync like for key: " + key, e);
//                // Можно добавить в очередь для повторной попытки
//                retryQueue.add(new SyncJob(key, newStatus));
//            }
//        }, asyncExecutor);
//    }
//
//    // === additional methods ===
//
//    private boolean isTrue(String value) {
//        if (value == null) {
//            return false;
//        }
//        String trimmed = value.trim();
//        return "true".equalsIgnoreCase(trimmed) ||
//                "1".equals(trimmed) ||
//                "yes".equalsIgnoreCase(trimmed);
//    }
//
//    private void sendSyncCommand(String key, boolean newStatus) {
//        String command = newStatus ?
//                "SET " + key + " true EX 2592000\r\n" :
//                "DELETE " + key + "\r\n";
//        asyncSyncToKryocache(key, command);
//    }
//
//    public long getBaseCountFromKryocache(String postId) {
//        String key = "like:count:" + postId;
//        String result = sendCommandWithResponse("GET" + key + "\r\n");
//
//        if (result == null || result.isEmpty() || "NOT_FOUNT".equals(result)) {
//            return 0L;
//        }
//
//        try {
//            return Long.parseLong(result);
//        } catch (NumberFormatException e) {
//            log.warn("");
//            return 0L;
//        }
//    }
//
//    /*
//    async sync
//     */
//    public void asyncSyncToKryocache(String key, String operation) {
//        Thread.ofVirtual().start(() -> {
//            try {
//                currentLoad.increment();
//                boolean success = false;
//
//                if (operation.equals("INCR") || operation.equals("DECR")) {
//                    String command = operation + " " + key + "\r\n";
//                    success = executeCommand(command);
//                } else {
//                    success = executeCommand(operation);
//                }
//
//                if (!success) {
//                    failedOperations.incrementAndGet();
//                    log.warn("Failed to sync like operation to Kryocache: {}", operation);
//                }
//            } finally {
//                currentLoad.decrement();
//            }
//        });
//    }
//
//    public void startSyncScheduler() {
//        Thread.ofVirtual().start(() -> {
//            while (true) {
//                try {
//                    Thread.sleep(5000);
//
//                    syncLocalCounters();
//                    syncLocalLikeStatus();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        });
//    }
//
//    /*
//    sum then reset that
//     */
//    public void syncLocalCounters() {
//        localCounters.forEach((key, adder) -> {
//            long delta = adder.sumThenReset(); // reset and sum
//            if (delta != 0) {
//                String command = delta > 0 ?
//                        "INCRBY " + key + " " + delta + "\r\n" :
//                        "DECRBY " + key + " " + (-delta) + "\r\n";
//                executeCommand(command);
//            }
//        });
//    }
//
//    // add
//    public void syncLocalLikeStatus() {
//        localLikeStatus.forEach((key, check) -> {
//
//        });
//    }
//
//    private boolean executeCommand(String command) {
//        lock.lock();
//        try {
//            for (int attempt = 0; attempt < maxRetries; attempt++) {
//                try {
//                    Socket socket = new Socket();
//
//                    socket.setSoTimeout(timeout);
//                    InetAddress[] addresses = InetAddress.getAllByName(host);
//
//                    List<InetAddress> ipv6Addresses = new ArrayList<>();
//
//                    for (InetAddress addr : addresses) {
//                        if (addr instanceof Inet6Address) {
//                            ipv6Addresses.add(addr);
//                        } else {
//                            return false;
//                        }
//                    }
//
//                    for (int i = 0; i < addresses.length; i++) {
//                        InetAddress addr = addresses[i];
//                        log.info("     [{}] {} (IPv6: {}, Loopback: {})",
//                                i, addr.getHostAddress(),
//                                addr instanceof Inet6Address,
//                                addr.isLoopbackAddress());
//                    }
//
//                    InetAddress ipv6Address = null;
//                    if (!ipv6Addresses.isEmpty()) {
//                        ipv6Address = ipv6Addresses.get(0);
//                    } else {
//                        throw new UnknownHostException("No IPv6 address found for host: " + host);
//                    }
//
//                    InetSocketAddress socketAddress = new InetSocketAddress(ipv6Address, port);
//
//                    socket.connect(socketAddress, timeout);
//
//                    try (OutputStream out = socket.getOutputStream();
//                         BufferedReader in = new BufferedReader(
//                                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
//
//                        byte[] commandBytes = command.getBytes(StandardCharsets.UTF_8);
//
//                        out.write(commandBytes);
//                        out.flush();
//
//                        String response = in.readLine();
//
//                        boolean success = response != null && response.contains("OK");
//
//                        socket.close();
//
//                        return success;
//                    }
//
//                } catch (SocketTimeoutException e) {
//                    log.error("   ⏰ Socket timeout after {} ms: {}", timeout, e.getMessage());
//                } catch (ConnectException e) {
//                    log.error("   🔌 Connection refused: {}", e.getMessage());
//                    log.error("   Possible causes:");
//                    log.error("     - Server is not running");
//                    log.error("     - Server is not listening on port {}", port);
//                    log.error("     - Firewall blocking connection");
//                } catch (UnknownHostException e) {
//                    log.error("   🏷️ Unknown host: {}", e.getMessage());
//                } catch (IOException e) {
//                    log.error("   ❌ I/O Error: {}", e.getMessage());
//                    log.error("   Error details:", e);
//                } catch (Exception e) {
//                    log.error("   ⚠️ Unexpected error: {}", e.getMessage());
//                    log.error("   Stack trace:", e);
//                }
//
//                log.warn("   Attempt {} failed for command '{}'",
//                        attempt + 1, command.trim());
//
//                if (attempt < maxRetries - 1) {
//                    int backoffMs = 100 * (1 << attempt);
//                    try {
//                        Thread.sleep(backoffMs);
//                    } catch (InterruptedException ie) {
//                        Thread.currentThread().interrupt();
//                        break;
//                    }
//                }
//            }
//
//            return false;
//
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    private String sendCommandWithResponse(String command) {
//        lock.lock();
//        try {
//            for (int attempt = 0; attempt < maxRetries; attempt++) {
//                long startTime = System.currentTimeMillis();
//
//                try {
//                    Socket socket = new Socket();
//                    socket.setSoTimeout(timeout);
//
//                    InetAddress[] addresses = InetAddress.getAllByName(host);
//                    InetAddress ipv6Address = null;
//
//                    for (InetAddress addr : addresses) {
//                        if (addr instanceof Inet6Address) {
//                            ipv6Address = addr;
//                            break;
//                        }
//                    }
//
//                    if (ipv6Address == null) {
//                        throw new UnknownHostException("No IPv6 address found for host: " + host);
//                    }
//
//                    InetSocketAddress socketAddress = new InetSocketAddress(ipv6Address, port);
//                    socket.connect(socketAddress, timeout);
//
//                    try (OutputStream out = socket.getOutputStream();
//                         BufferedReader in = new BufferedReader(
//                                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
//
//                        byte[] commandBytes = command.getBytes(StandardCharsets.UTF_8);
//                        out.write(commandBytes);
//                        out.flush();
//
//                        StringBuilder response = new StringBuilder();
//                        char[] buffer = new char[8192]; // Увеличить буфер
//                        int charsRead;
//
//                        // Читаем всё что есть, пока не кончится
//                        while ((charsRead = in.read(buffer)) != -1) {
//                            response.append(buffer, 0, charsRead);
//                            // Проверяем, не закончился ли ответ (по таймауту или признаку конца)
//                            if (!in.ready()) {
//                                Thread.sleep(100); // Даем время для получения оставшихся данных
//                                if (!in.ready()) {
//                                    break;
//                                }
//                            }
//                        }
//
//                        String responseStr = response.toString().trim();
//                        log.info("DEBUG [sendCommandWithResponse]: Full response ({} chars): '{}'",
//                                responseStr.length(), responseStr);
//
//                        socket.close();
//                        return responseStr;
//                    }
//
//                } catch (Exception e) {
//                    log.error("   ⚠️ Error: {}", e.getMessage());
//                }
//
//                if (attempt < maxRetries - 1) {
//                    int backoffMs = 100 * (1 << attempt);
//                    try {
//                        Thread.sleep(backoffMs);
//                    } catch (InterruptedException ie) {
//                        Thread.currentThread().interrupt();
//                        break;
//                    }
//                }
//            }
//
//            return null;
//
//        } finally {
//            lock.unlock();
//        }
//    }
//}
