package com.posts.post.infrastructure.metrics;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

@Component
public class MetricsFileLogger implements Filter {

    private static final Path METRICS_LOG = Path.of("logs/metrics.log");
    private static final Path STATS_LOG = Path.of("logs/stats.log");
    private final RequestMetrics metrics;

    public MetricsFileLogger(RequestMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        if (!threadMXBean.isThreadCpuTimeSupported()) {
            throw new RuntimeException("CPU time measurement not supported!");
        }
        threadMXBean.setThreadCpuTimeEnabled(true);
        long startWallTime = System.nanoTime();
        long startCpuTime = threadMXBean.getCurrentThreadCpuTime();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            long endCpuTime = threadMXBean.getCurrentThreadCpuTime();
            long endWallTime = System.nanoTime();
            String endpoint = req.getRequestURI();

            long cpuTimeUsed = endCpuTime - startCpuTime;
            long wallTimeUsed = endWallTime - startWallTime;
            double cpuUsagePercent = (cpuTimeUsed * 100.0) / wallTimeUsed;

            String rawLog = String.format("[%s] %s %s - %d ms\n CPU Usage: %.2f%% (CPU time: %d ns, Wall time: %d ns)",
                    LocalDateTime.now(),
                    req.getMethod(),
                    endpoint,
                    duration,
                    cpuUsagePercent,
                    cpuTimeUsed,
                    wallTimeUsed);

            writeToFile(METRICS_LOG, rawLog);

            metrics.recordReq(endpoint, duration);
            RequestMetrics.Stats stats = metrics.getStats(endpoint);
            writeToFile(STATS_LOG, stats.toString() + "\n");
        }
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(
                path,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}