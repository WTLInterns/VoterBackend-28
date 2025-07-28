package com.votersystem.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.votersystem.controller.UserController;
import com.votersystem.entity.Agent;
import com.votersystem.entity.Transaction;
import com.votersystem.entity.User;
import com.votersystem.repository.AgentRepository;
import com.votersystem.repository.TransactionRepository;
import com.votersystem.repository.UserRepository;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> getUsersBySubAdmin(String subAdminUsername, Pageable pageable) {
        // Sub-admin creates users directly, so return users created by the sub-admin
        List<String> createdByList = List.of(subAdminUsername);
        return userRepository.findByCreatedByIn(createdByList, pageable);
    }

    public Page<User> getUsersForAgent(String agentUsername, Pageable pageable) {
        // Agent sees all users created by their sub-admin
        // First find which sub-admin created this agent
        Agent agent = agentRepository.findByMobile(agentUsername)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        String subAdminUsername = agent.getCreatedBy();
        if (subAdminUsername == null) {
            return Page.empty(pageable);
        }

        // Return all users created by this sub-admin
        List<String> createdByList = List.of(subAdminUsername);
        return userRepository.findByCreatedByIn(createdByList, pageable);
    }

    // Get all users for agent (mobile app version - returns List instead of Page)
    public List<User> getUsersForAgent(String agentUsername) {
        // Agent sees all users created by their sub-admin
        // First find which sub-admin created this agent
        Agent agent = agentRepository.findByMobile(agentUsername)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        String subAdminUsername = agent.getCreatedBy();
        if (subAdminUsername == null) {
            return List.of();
        }

        // Return all users created by this sub-admin
        return userRepository.findByCreatedBy(subAdminUsername);
    }

    // Advanced search users for agent (mobile app)
    public List<User> searchUsersForAgent(String agentUsername, String firstName, String lastName,
                                        Integer age, User.Gender gender, String vidhansabhaNo,
                                        String vibhaghKramank, Boolean paid, int limit) {
        // Agent sees all users created by their sub-admin with search filters
        // First find which sub-admin created this agent
        Agent agent = agentRepository.findByMobile(agentUsername)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        String subAdminUsername = agent.getCreatedBy();
        if (subAdminUsername == null) {
            return List.of();
        }

        // Search users created by this sub-admin with filters
        List<String> createdByList = List.of(subAdminUsername);
        Pageable pageable = PageRequest.of(0, limit);

        Page<User> userPage = userRepository.searchUsersFilteredByCreatedBy(
            firstName, lastName, age, gender, vidhansabhaNo, vibhaghKramank, paid,
            createdByList, pageable);

        return userPage.getContent();
    }
    
    public List<User> searchUsersByLastName(String lastName) {
        return userRepository.findByLastNameContainingIgnoreCase(lastName);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public User createUser(UserController.CreateUserRequest request, String createdBy) {
        User user = new User(
            request.getFirstName(),
            request.getLastName(),
            request.getAge(),
            request.getGender(),
            request.getVidhansabhaNo(),
            request.getVibhaghKramank(),
            request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO
        );

        // Set who created this user
        user.setCreatedBy(createdBy);

        return userRepository.save(user);
    }
    
    public User updateUser(Long id, UserController.UpdateUserRequest request) {
        User user = getUserById(id);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setVidhansabhaNo(request.getVidhansabhaNo());
        user.setVibhaghKramank(request.getVibhaghKramank());
        if (request.getAmount() != null) {
            user.setAmount(request.getAmount());
        }

        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
    
    public User markUserAsPaid(Long userId, String agentUsername, BigDecimal amount) {
        User user = getUserById(userId);
        
        if (user.getPaid()) {
            throw new RuntimeException("User is already marked as paid");
        }
        
        // Verify agent exists and is active
        Agent agent = agentRepository.findByUsername(agentUsername)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        if (agent.getStatus() != Agent.AgentStatus.ACTIVE) {
            throw new RuntimeException("Agent is not active");
        }
        
        // Mark user as paid
        user.markAsPaid(agentUsername, amount);
        user = userRepository.save(user);
        
        // Update agent statistics - recalculate from transaction data to ensure accuracy
        // This will properly calculate today's vs total amounts
        BigDecimal totalAmount = transactionRepository.getTotalAmountByAgent(agent.getId());
        BigDecimal todayAmount = transactionRepository.getTodaysTotalAmountByAgent(agent.getId());

        // Add the current transaction amount to the calculations
        totalAmount = totalAmount.add(amount);
        todayAmount = todayAmount.add(amount);

        agent.setTotalPayments(totalAmount.intValue());
        agent.setPaymentsToday(todayAmount.intValue());
        agentRepository.save(agent);
        
        // Create transaction record
        Transaction transaction = new Transaction(
            generateTransactionId(),
            userId,
            agent.getId(),
            amount,
            null, // location removed
            null, // latitude removed
            null  // longitude removed
        );
        transactionRepository.save(transaction);
        
        return user;
    }
    
    public Page<User> getUsersByPaymentStatus(Boolean paid, Pageable pageable) {
        return userRepository.findByPaid(paid, pageable);
    }
    
    public UserController.UserStatistics getUserStatistics() {
        Long totalUsers = userRepository.countTotalUsers();
        Long paidUsers = userRepository.countPaidUsers();
        Long unpaidUsers = userRepository.countUnpaidUsers();
        Double totalAmountCollected = userRepository.getTotalAmountCollected();
        Long usersPaidToday = userRepository.countUsersPaidToday();
        
        return new UserController.UserStatistics(
            totalUsers, paidUsers, unpaidUsers, totalAmountCollected, usersPaidToday
        );
    }
    
    public List<User> getUsersPaidToday() {
        return userRepository.getUsersPaidToday();
    }
    
    public List<User> getUsersPaidTodayByAgent(String agentUsername) {
        return userRepository.getUsersPaidTodayByAgent(agentUsername);
    }
    
    public List<User> getUsersPaidByAgent(String agentUsername) {
        return userRepository.findByPaidBy(agentUsername);
    }
    
    public List<User> getUsersPaidBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.findByPaidDateBetween(startDate, endDate);
    }
    
    public Double getAmountCollectedByAgent(String agentUsername) {
        return userRepository.getAmountCollectedByAgent(agentUsername);
    }
    
    public Long countUsersPaidTodayByAgent(String agentUsername) {
        return userRepository.countUsersPaidTodayByAgent(agentUsername);
    }
    
    public Page<User> searchUsers(String firstName, String lastName, Integer age, User.Gender gender,
                                Boolean paid, Pageable pageable) {
        return userRepository.searchUsers(firstName, lastName, age, gender, null, null, paid, pageable);
    }

    public Page<User> searchUsers(String firstName, String lastName, Integer age, User.Gender gender,
                                String vidhansabhaNo, String vibhaghKramank, Boolean paid,
                                int page, int size, Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);

        // Apply role-based filtering
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            // Sub-admin: see users they created directly
            List<String> createdByList = List.of(username);
            return userRepository.searchUsersFilteredByCreatedBy(firstName, lastName, age, gender,
                                                               vidhansabhaNo, vibhaghKramank, paid,
                                                               createdByList, pageable);
        } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_AGENT"))) {
            // Agent: see users created by their sub-admin
            Agent agent = agentRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            String subAdminUsername = agent.getCreatedBy();
            if (subAdminUsername == null) {
                return Page.empty(pageable);
            }

            List<String> createdByList = List.of(subAdminUsername);
            return userRepository.searchUsersFilteredByCreatedBy(firstName, lastName, age, gender,
                                                               vidhansabhaNo, vibhaghKramank, paid,
                                                               createdByList, pageable);
        } else {
            // Master admin: see all users
            return userRepository.searchUsers(firstName, lastName, age, gender, vidhansabhaNo, vibhaghKramank, paid, pageable);
        }
    }

    public List<User> searchUsersForMobile(String firstName, String lastName, Integer age, User.Gender gender,
                                         String vidhansabhaNo, String vibhaghKramank, Boolean paid,
                                         int limit, Authentication authentication) {
        Pageable pageable = PageRequest.of(0, limit);

        // Apply role-based filtering
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            // Sub-admin: see users they created directly
            List<String> createdByList = List.of(username);
            Page<User> page = userRepository.searchUsersFilteredByCreatedBy(firstName, lastName, age, gender,
                                                                          vidhansabhaNo, vibhaghKramank, paid,
                                                                          createdByList, pageable);
            return page.getContent();
        } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_AGENT"))) {
            // Agent: see users created by their sub-admin
            Agent agent = agentRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            String subAdminUsername = agent.getCreatedBy();
            if (subAdminUsername == null) {
                return List.of();
            }

            List<String> createdByList = List.of(subAdminUsername);
            Page<User> page = userRepository.searchUsersFilteredByCreatedBy(firstName, lastName, age, gender,
                                                                          vidhansabhaNo, vibhaghKramank, paid,
                                                                          createdByList, pageable);
            return page.getContent();
        } else {
            // Master admin: see all users
            Page<User> page = userRepository.searchUsers(firstName, lastName, age, gender, vidhansabhaNo, vibhaghKramank, paid, pageable);
            return page.getContent();
        }
    }

    public List<User> getRecentUsers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return userRepository.getRecentUsers(thirtyDaysAgo);
    }
    
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
