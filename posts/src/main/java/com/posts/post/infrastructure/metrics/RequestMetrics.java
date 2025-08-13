package com.posts.post.infrastructure.metrics;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestMetrics {
    private final ConcurrentHashMap<String, List<Long>> metrics = new ConcurrentHashMap<>();

    /*
    * computeIfAbsent — if there is no list for endpoint, creates a new one.

    Collections.synchronizedList — makes the list thread-safe.

    .add(durationMs) — adds the query execution time.
    * */
    public void recordReq(String endpoint, long durationMs) {
        metrics.computeIfAbsent(endpoint, k -> Collections.synchronizedList(new ArrayList<>())).add(durationMs);
    }

    public Stats getStats(String endpoint) {
        List<Long> durations = metrics.getOrDefault(endpoint, Collections.emptyList());
        if (durations.isEmpty()) return new Stats(0, 0, 0, 0, 0, 0, 0, 0, 0);

        List<Long> sorted = new ArrayList<>(durations);
        Collections.sort(sorted);

        double mean = sorted.stream().mapToLong(v -> v).average().orElse(0);
        double variance = sorted.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        return new Stats(
                sorted.size(),
                mean,
                stdDev,
                sorted.get(sorted.size()/2),          // median
                getPercentile(sorted, 0.90),         // p90
                getPercentile(sorted, 0.95),         // p95
                getPercentile(sorted, 0.99),         // p99
                sorted.get(0),                       // min
                sorted.get(sorted.size()-1)          // max
        );
    }

    private long getPercentile(List<Long> data, double percentile) {
        int index = (int) Math.ceil(percentile * data.size()) - 1;
        return data.get(Math.max(0, index));
    }

    public record Stats(
            int count,
            double mean,
            double stdDev,
            long median,
            long p90,
            long p95,
            long p99,
            long min,
            long max
    ) {
        @Override
        public String toString() {
            return String.format(
                    "[%s] Count: %d | Avg: %.2fms | StdDev: %.2f | " +
                            "Median: %dms | P90: %dms | P95: %dms | P99: %dms | " +
                            "Min: %dms | Max: %dms",
                    LocalDateTime.now(),
                    count, mean, stdDev,
                    median, p90, p95, p99,
                    min, max
            );
        }
    }
}
