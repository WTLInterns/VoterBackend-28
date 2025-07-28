package com.votersystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.votersystem.entity.Agent;
import com.votersystem.entity.AgentLocation;
import com.votersystem.repository.AgentRepository;
import com.votersystem.repository.AgentLocationRepository;
import com.votersystem.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ScheduledTaskService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AgentLocationRepository agentLocationRepository;

    /**
     * Reset today's payments for all agents at midnight (00:00:00)
     * This ensures that "Today's Distribution" only shows current day's payments
     */
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void resetTodaysPayments() {
        System.out.println("=== SCHEDULED TASK: Resetting today's payments at midnight ===");
        
        List<Agent> allAgents = agentRepository.findAll();
        
        for (Agent agent : allAgents) {
            // Recalculate today's payments from transaction data (should be 0 at midnight)
            BigDecimal todayAmount = transactionRepository.getTodaysTotalAmountByAgent(agent.getId());
            agent.setPaymentsToday(todayAmount != null ? todayAmount.intValue() : 0);
            
            agentRepository.save(agent);
            
            System.out.println("Reset today's payments for agent " + agent.getId() + 
                             " - Today: ₹" + (todayAmount != null ? todayAmount : 0));
        }
        
        System.out.println("=== COMPLETED: Today's payments reset for " + allAgents.size() + " agents ===");
    }
    
    /**
     * Recalculate all agent payment totals every hour to ensure data accuracy
     * This handles any discrepancies that might occur
     */
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void recalculateAgentPayments() {
        System.out.println("=== SCHEDULED TASK: Hourly agent payment recalculation ===");
        
        List<Agent> allAgents = agentRepository.findAll();
        
        for (Agent agent : allAgents) {
            // Get accurate totals from transaction data
            BigDecimal totalAmount = transactionRepository.getTotalAmountByAgent(agent.getId());
            BigDecimal todayAmount = transactionRepository.getTodaysTotalAmountByAgent(agent.getId());
            
            // Update agent's payment fields
            agent.setTotalPayments(totalAmount != null ? totalAmount.intValue() : 0);
            agent.setPaymentsToday(todayAmount != null ? todayAmount.intValue() : 0);
            
            agentRepository.save(agent);
        }
        
        System.out.println("=== COMPLETED: Payment recalculation for " + allAgents.size() + " agents ===");
    }

    /**
     * Check for agents that haven't sent location updates recently and mark them as offline
     * Runs every 30 seconds to ensure timely offline detection
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void detectOfflineAgents() {
        try {
            // Consider agents offline if they haven't sent updates in the last 15 seconds
            LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(15);

            List<AgentLocation> staleLocations = agentLocationRepository.findStaleLocations(cutoffTime);

            int offlineCount = 0;
            for (AgentLocation location : staleLocations) {
                // Only update if currently marked as ONLINE
                if (location.getConnectionStatus() == AgentLocation.ConnectionStatus.ONLINE) {
                    agentLocationRepository.updateConnectionStatus(
                        location.getAgentId(),
                        AgentLocation.ConnectionStatus.OFFLINE,
                        LocalDateTime.now()
                    );
                    offlineCount++;
                    System.out.println("Marked agent " + location.getAgentId() + " as OFFLINE due to inactivity");
                }
            }

            if (offlineCount > 0) {
                System.out.println("=== OFFLINE DETECTION: Marked " + offlineCount + " agents as offline ===");
            }

        } catch (Exception e) {
            System.err.println("Error in offline detection task: " + e.getMessage());
        }
    }
}
