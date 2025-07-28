package com.votersystem.service;

import com.votersystem.controller.TransactionController;
import com.votersystem.entity.Transaction;
import com.votersystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        try {
            return transactionRepository.findAllWithUserAndAgentOrderByCreatedAtDesc(pageable);
        } catch (Exception e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch transactions: " + e.getMessage(), e);
        }
    }
    
    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }
    
    public List<Transaction> getTransactionsByAgent(String agentId) {
        return transactionRepository.findByAgentId(agentId);
    }
    
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
    
    public List<Transaction> getTodaysTransactions() {
        return transactionRepository.findTodaysTransactions();
    }
    
    public List<Transaction> getTransactionsByDateRange(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        return transactionRepository.findByCreatedAtBetween(start, end);
    }
    
    public TransactionController.TransactionStatistics getTransactionStatistics() {
        Long totalTransactions = transactionRepository.countTotalTransactions();
        Long completedTransactions = transactionRepository.countByStatus(Transaction.TransactionStatus.COMPLETED);
        Long pendingTransactions = transactionRepository.countByStatus(Transaction.TransactionStatus.PENDING);
        Long failedTransactions = transactionRepository.countByStatus(Transaction.TransactionStatus.FAILED);
        BigDecimal totalAmount = transactionRepository.getTotalAmount();
        BigDecimal todayAmount = transactionRepository.getTodaysTotalAmount();
        Long todayTransactions = transactionRepository.countTodaysTransactions();
        
        return new TransactionController.TransactionStatistics(
            totalTransactions, completedTransactions, pendingTransactions, failedTransactions,
            totalAmount, todayAmount, todayTransactions
        );
    }
    
    public List<TransactionController.MonthlyStats> getMonthlyStatistics() {
        List<Object[]> results = transactionRepository.getMonthlyStatistics();
        
        return results.stream().map(result -> {
            int year = ((Number) result[0]).intValue();
            int month = ((Number) result[1]).intValue();
            Long count = ((Number) result[2]).longValue();
            BigDecimal amount = (BigDecimal) result[3];

            return new TransactionController.MonthlyStats(year, month, count, amount);
        }).collect(Collectors.toList());
    }

    // Create sample transaction for testing
    public Transaction createSampleTransaction(TransactionController.CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setId("TXN-" + System.currentTimeMillis());
        transaction.setUserId(request.getUserId());
        transaction.setAgentId(request.getAgentId());
        transaction.setAmount(request.getAmount());
        transaction.setLocation(request.getLocation());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCreatedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    // Initialize sample transaction data for testing
    public void initSampleTransactionData() {
        // Create sample transactions for testing
        String[] locations = {
            "Mumbai Central Station",
            "Bandra West, Mumbai",
            "Andheri East, Mumbai",
            "Powai, Mumbai",
            "Thane West"
        };

        for (int i = 0; i < locations.length; i++) {
            Transaction transaction = new Transaction();
            transaction.setId("TXN-SAMPLE-" + (System.currentTimeMillis() + i));
            transaction.setUserId((long) (i + 1));
            transaction.setAgentId("AGENT00" + (i + 1));
            transaction.setAmount(new BigDecimal(100 + (i * 50)));
            transaction.setLocation(locations[i]);
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCreatedAt(LocalDateTime.now().minusHours(i));

            transactionRepository.save(transaction);
        }
    }
    
    public List<Transaction> getTodaysTransactionsByAgent(String agentId) {
        return transactionRepository.findTodaysTransactionsByAgent(agentId);
    }
    
    public BigDecimal getTotalAmountByAgent(String agentId) {
        return transactionRepository.getTotalAmountByAgent(agentId);
    }
    
    public BigDecimal getTodaysTotalAmountByAgent(String agentId) {
        return transactionRepository.getTodaysTotalAmountByAgent(agentId);
    }
    
    public Long countTodaysTransactionsByAgent(String agentId) {
        return transactionRepository.countTodaysTransactionsByAgent(agentId);
    }
}
