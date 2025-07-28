package com.votersystem.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.votersystem.controller.AgentController;
import com.votersystem.entity.Agent;
import com.votersystem.entity.User;
import com.votersystem.repository.AgentRepository;
import com.votersystem.repository.TransactionRepository;
import com.votersystem.repository.UserRepository;

@Service
@Transactional
public class AgentService {
    
    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Email service disabled - credentials provided manually
    // @Autowired
    // private EmailService emailService;
    
    public Agent getAgentByMobile(String mobile) {
        return agentRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("Agent not found with mobile: " + mobile));
    }

    // Compatibility method - delegates to mobile
    public Agent getAgentByUsername(String username) {
        return getAgentByMobile(username);
    }
    
    public Agent getAgentById(String id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent not found with id: " + id));
    }
    
    public List<Agent> getAllAgents() {
        List<Agent> agents = agentRepository.findAll();
        // Recalculate payment totals for all agents to ensure accuracy
        for (Agent agent : agents) {
            recalculateAgentPayments(agent);
        }
        return agents;
    }
    
    public List<Agent> getActiveAgents() {
        return agentRepository.findActiveAgents();
    }
    
    public List<Agent> getAgentsByCreator(String createdBy) {
        return agentRepository.findByCreatedBy(createdBy);
    }
    
    public Agent createAgent(String firstName, String lastName, String mobile,
                           String password, String createdBy) {

        // Validate input parameters
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required and cannot be empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required and cannot be empty");
        }
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required and cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required and cannot be empty");
        }

        // Validate mobile number format - exactly 10 digits starting with 6-9
        String cleanedMobile = mobile.replaceAll("[^\\d]", "");
        if (cleanedMobile.length() != 10) {
            throw new IllegalArgumentException("Mobile number must be exactly 10 digits");
        }
        if (!cleanedMobile.matches("^[6-9]\\d{9}$")) {
            throw new IllegalArgumentException("Mobile number must start with 6, 7, 8, or 9");
        }

        // Check if mobile already exists
        if (agentRepository.existsByMobile(mobile)) {
            throw new IllegalArgumentException("An agent with mobile number " + mobile + " already exists. Please use a different mobile number.");
        }

        // Generate agent ID
        String agentId = generateAgentId();

        Agent agent = new Agent(
            agentId,
            firstName,
            lastName,
            mobile,
            passwordEncoder.encode(password),
            createdBy
        );
        
        agent = agentRepository.save(agent);

        // Email sending disabled - credentials will be provided manually
        // emailService.sendAgentCredentials(email, firstName, username, password);

        return agent;
    }
    
    public Agent updateAgent(String id, String firstName, String lastName, String mobile) {
        Agent agent = getAgentById(id);

        // Check if mobile is being changed and if new value already exists
        if (!agent.getMobile().equals(mobile) && agentRepository.existsByMobile(mobile)) {
            throw new RuntimeException("Agent with this mobile number already exists");
        }

        agent.setFirstName(firstName);
        agent.setLastName(lastName);
        agent.setMobile(mobile);

        return agentRepository.save(agent);
    }

    // New method to support password updates
    public Agent updateAgent(String id, String firstName, String lastName, String mobile, String password) {
        Agent agent = getAgentById(id);

        // Check if mobile is being changed and if new value already exists
        if (mobile != null && !agent.getMobile().equals(mobile) && agentRepository.existsByMobile(mobile)) {
            throw new RuntimeException("Agent with this mobile number already exists");
        }

        // Update basic fields
        if (firstName != null) {
            agent.setFirstName(firstName);
        }
        if (lastName != null) {
            agent.setLastName(lastName);
        }
        if (mobile != null) {
            agent.setMobile(mobile);
        }

        // Update password if provided
        if (password != null && !password.trim().isEmpty()) {
            agent.setPasswordHash(passwordEncoder.encode(password));
        }

        return agentRepository.save(agent);
    }

    // Update agent interface status
    public Agent updateAgentInterfaceStatus(String id, Integer interfaceStatus) {
        Agent agent = getAgentById(id);

        // Validate interface status
        if (interfaceStatus == null || (interfaceStatus != 1 && interfaceStatus != 2)) {
            throw new RuntimeException("Interface status must be 1 (Money Distribution) or 2 (Issue Reporting)");
        }

        agent.setInterfaceStatus(interfaceStatus);
        return agentRepository.save(agent);
    }



    public void blockAgent(String id) {
        Agent agent = getAgentById(id);
        agent.setStatus(Agent.AgentStatus.BLOCKED);
        agentRepository.save(agent);
    }
    
    public void unblockAgent(String id) {
        Agent agent = getAgentById(id);
        agent.setStatus(Agent.AgentStatus.ACTIVE);
        agentRepository.save(agent);
    }

    public void deleteAgent(String id) {
        Agent agent = getAgentById(id);
        agentRepository.delete(agent);
    }
    

    
    public List<User> getUnpaidUsers() {
        return userRepository.findByPaid(false);
    }
    
    public AgentController.AgentStatistics getAgentStatistics(String username) {
        Agent agent = getAgentByUsername(username);
        
        Double amountCollectedToday = userRepository.getAmountCollectedByAgent(username);
        Double totalAmountCollected = userRepository.getAmountCollectedByAgent(username);
        
        return new AgentController.AgentStatistics(
            agent.getPaymentsToday(),
            agent.getTotalPayments(),
            amountCollectedToday,
            totalAmountCollected
        );
    }
    

    
    public void resetDailyPaymentCounts() {
        List<Agent> agents = agentRepository.findAll();
        for (Agent agent : agents) {
            agent.setPaymentsToday(0);
        }
        agentRepository.saveAll(agents);
    }

    public Agent saveAgent(Agent agent) {
        return agentRepository.save(agent);
    }
    
    private String generateAgentId() {
        Integer maxNumber = agentRepository.findMaxAgentNumber();
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("AGENT%03d", nextNumber);
    }

    /**
     * Recalculate payment totals for all agents from transaction data
     */
    public void recalculateAllAgentPayments() {
        List<Agent> allAgents = agentRepository.findAll();
        for (Agent agent : allAgents) {
            recalculateAgentPayments(agent);
        }
    }

    /**
     * Recalculate payment totals for a specific agent
     */
    private void recalculateAgentPayments(Agent agent) {
        // Get total amount distributed by this agent
        BigDecimal totalAmount = transactionRepository.getTotalAmountByAgent(agent.getId());
        BigDecimal todayAmount = transactionRepository.getTodaysTotalAmountByAgent(agent.getId());

        // Update agent's payment fields
        agent.setTotalPayments(totalAmount != null ? totalAmount.intValue() : 0);
        agent.setPaymentsToday(todayAmount != null ? todayAmount.intValue() : 0);

        // Save to database
        agentRepository.save(agent);

        System.out.println("Updated agent " + agent.getId() +
                          " - Total: ₹" + (totalAmount != null ? totalAmount : 0) +
                          ", Today: ₹" + (todayAmount != null ? todayAmount : 0));
    }

    /**
     * Get agent IDs created by specific admin (for location tracking authorization)
     */
    public List<String> getAgentIdsByCreatedBy(String createdBy) {
        try {
            List<Agent> agents = agentRepository.findByCreatedBy(createdBy);
            return agents.stream()
                    .map(Agent::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting agent IDs by created by: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get total agent count (for location statistics)
     */
    public long getTotalAgentCount() {
        try {
            return agentRepository.count();
        } catch (Exception e) {
            System.err.println("Error getting total agent count: " + e.getMessage());
            return 0;
        }
    }
}
