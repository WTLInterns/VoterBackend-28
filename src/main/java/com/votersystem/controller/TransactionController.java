package com.votersystem.controller;

import com.votersystem.dto.TransactionResponse;
import com.votersystem.entity.Transaction;
import com.votersystem.service.TransactionService;
import com.votersystem.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    // Get all transactions with pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactions = transactionService.getAllTransactions(pageable);

            // Convert to TransactionResponse DTOs
            Page<TransactionResponse> transactionResponses = transactions.map(TransactionResponse::new);

            return ResponseEntity.ok(ApiResponse.success(transactionResponses, "Transactions retrieved successfully"));
        } catch (Exception e) {
            System.err.println("TransactionController: Error in getAllTransactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to retrieve transactions: " + e.getMessage())
            );
        }
    }
    
    // Get transaction by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Transaction>> getTransactionById(@PathVariable String id) {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(transaction, "Transaction retrieved successfully"));
    }
    
    // Get transactions by agent
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionsByAgent(@PathVariable String agentId) {
        List<Transaction> transactions = transactionService.getTransactionsByAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(transactions, "Agent transactions retrieved successfully"));
    }
    
    // Get transactions by user
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionsByUser(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(transactions, "User transactions retrieved successfully"));
    }
    
    // Get today's transactions
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTodaysTransactions() {
        List<Transaction> transactions = transactionService.getTodaysTransactions();
        return ResponseEntity.ok(ApiResponse.success(transactions, "Today's transactions retrieved successfully"));
    }
    
    // Get transaction statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<TransactionStatistics>> getTransactionStatistics() {
        TransactionStatistics stats = transactionService.getTransactionStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Transaction statistics retrieved successfully"));
    }
    
    // Get transactions by date range
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transactions retrieved successfully"));
    }
    
    // Get monthly statistics
    @GetMapping("/monthly-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<MonthlyStats>>> getMonthlyStatistics() {
        List<MonthlyStats> stats = transactionService.getMonthlyStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Monthly statistics retrieved successfully"));
    }

    // Create sample transaction (for testing without mobile app)
    @PostMapping("/create-sample")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Transaction>> createSampleTransaction(
            @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createSampleTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(transaction, "Sample transaction created successfully"));
    }

    // Initialize sample transaction data (for testing)
    @PostMapping("/init-sample-data")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> initSampleTransactionData() {
        transactionService.initSampleTransactionData();
        return ResponseEntity.ok(ApiResponse.success("Sample transaction data created", "Sample transaction data initialized"));
    }
    
    // DTOs
    public static class CreateTransactionRequest {
        private Long userId;
        private String agentId;
        private BigDecimal amount;
        private String location;

        // Constructors
        public CreateTransactionRequest() {}

        public CreateTransactionRequest(Long userId, String agentId, BigDecimal amount, String location) {
            this.userId = userId;
            this.agentId = agentId;
            this.amount = amount;
            this.location = location;
        }

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class TransactionStatistics {
        private Long totalTransactions;
        private Long completedTransactions;
        private Long pendingTransactions;
        private Long failedTransactions;
        private BigDecimal totalAmount;
        private BigDecimal todayAmount;
        private Long todayTransactions;
        
        public TransactionStatistics(Long totalTransactions, Long completedTransactions, 
                                   Long pendingTransactions, Long failedTransactions,
                                   BigDecimal totalAmount, BigDecimal todayAmount, Long todayTransactions) {
            this.totalTransactions = totalTransactions;
            this.completedTransactions = completedTransactions;
            this.pendingTransactions = pendingTransactions;
            this.failedTransactions = failedTransactions;
            this.totalAmount = totalAmount;
            this.todayAmount = todayAmount;
            this.todayTransactions = todayTransactions;
        }
        
        // Getters and setters
        public Long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Long totalTransactions) { this.totalTransactions = totalTransactions; }
        public Long getCompletedTransactions() { return completedTransactions; }
        public void setCompletedTransactions(Long completedTransactions) { this.completedTransactions = completedTransactions; }
        public Long getPendingTransactions() { return pendingTransactions; }
        public void setPendingTransactions(Long pendingTransactions) { this.pendingTransactions = pendingTransactions; }
        public Long getFailedTransactions() { return failedTransactions; }
        public void setFailedTransactions(Long failedTransactions) { this.failedTransactions = failedTransactions; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getTodayAmount() { return todayAmount; }
        public void setTodayAmount(BigDecimal todayAmount) { this.todayAmount = todayAmount; }
        public Long getTodayTransactions() { return todayTransactions; }
        public void setTodayTransactions(Long todayTransactions) { this.todayTransactions = todayTransactions; }
    }
    
    public static class MonthlyStats {
        private int year;
        private int month;
        private Long transactionCount;
        private BigDecimal totalAmount;
        
        public MonthlyStats(int year, int month, Long transactionCount, BigDecimal totalAmount) {
            this.year = year;
            this.month = month;
            this.transactionCount = transactionCount;
            this.totalAmount = totalAmount;
        }
        
        // Getters and setters
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        public Long getTransactionCount() { return transactionCount; }
        public void setTransactionCount(Long transactionCount) { this.transactionCount = transactionCount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}
