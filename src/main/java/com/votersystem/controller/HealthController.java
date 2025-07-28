package com.votersystem.controller;

import com.votersystem.repository.TransactionRepository;
import com.votersystem.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Voter Registration System");
        health.put("version", "1.0.0");

        // Check database connection
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", "connected");
        } catch (Exception e) {
            health.put("database", "error: " + e.getMessage());
        }

        // Check transaction table
        try {
            Long count = transactionRepository.countTotalTransactions();
            health.put("transactionTable", "accessible");
            health.put("transactionCount", count);
        } catch (Exception e) {
            health.put("transactionTable", "error: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(health, "Service is healthy"));
    }

    @GetMapping("/health/transactions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> transactionHealthCheck() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Test the problematic query
            transactionRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, 1));
            result.put("paginationQuery", "working");
            result.put("status", "healthy");
            return ResponseEntity.ok(ApiResponse.success(result, "Transaction health check completed"));

        } catch (Exception e) {
            result.put("status", "unhealthy");
            result.put("error", e.getMessage());
            result.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(ApiResponse.error("Transaction health check failed"));
        }
    }
}
