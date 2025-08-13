package com.posts.post.infrastructure.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/metrics")
public class MetricsController {
    private final RequestMetrics metrics;

    public MetricsController(RequestMetrics metrics) {
        this.metrics = metrics;
    }

    @GetMapping("/{endpoint}")
    public ResponseEntity<?> getMetrics(@PathVariable String endpoint) {
        try {
            RequestMetrics.Stats stats = metrics.getStats("/" + endpoint); // Добавляем / перед endpoint
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", "Failed to get metrics",
                            "details", e.getMessage()
                    ));
        }
    }
}