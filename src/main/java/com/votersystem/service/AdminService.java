package com.votersystem.service;

import com.votersystem.controller.AdminController;
import com.votersystem.entity.Administrator;
import com.votersystem.entity.Agent;
import com.votersystem.repository.AdministratorRepository;
import com.votersystem.repository.AgentRepository;
import com.votersystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AdminService {
    
    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Autowired
    // private EmailService emailService;
    
    public Administrator createSubAdmin(String firstName, String lastName, String mobile,
                                      String password, String createdBy) {
        // Check if mobile already exists
        if (administratorRepository.existsByMobile(mobile)) {
            throw new RuntimeException("Administrator with this mobile number already exists");
        }

        // Generate admin ID
        String adminId = generateAdminId();

        Administrator subAdmin = new Administrator(
            adminId,
            firstName,
            lastName,
            mobile,
            passwordEncoder.encode(password),
            Administrator.AdminRole.ADMIN,
            createdBy
        );
        
        subAdmin = administratorRepository.save(subAdmin);

        // Email sending disabled - credentials will be provided manually
        // emailService.sendSubAdminCredentials(email, firstName, username, password);

        return subAdmin;
    }
    
    public List<Administrator> getAllSubAdmins() {
        List<Administrator> subAdmins = administratorRepository.findSubAdmins();
        // Calculate and update total payments for each admin
        updateTotalPaymentsForAllAdmins(subAdmins);
        return subAdmins;
    }
    
    public Administrator getSubAdminById(String id) {
        return administratorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sub-admin not found with id: " + id));
    }
    
    public Administrator updateSubAdmin(String id, AdminController.UpdateSubAdminRequest request) {
        Administrator subAdmin = getSubAdminById(id);

        // Check if mobile is being changed and if new mobile already exists
        if (request.getMobile() != null && !subAdmin.getMobile().equals(request.getMobile()) &&
            administratorRepository.existsByMobile(request.getMobile())) {
            throw new RuntimeException("Administrator with this mobile number already exists");
        }

        // Update basic fields
        if (request.getFirstName() != null) {
            subAdmin.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            subAdmin.setLastName(request.getLastName());
        }
        if (request.getMobile() != null) {
            subAdmin.setMobile(request.getMobile());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            subAdmin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return administratorRepository.save(subAdmin);
    }
    
    public void blockSubAdmin(String id) {
        Administrator subAdmin = getSubAdminById(id);
        subAdmin.setStatus(Administrator.AdminStatus.BLOCKED);
        administratorRepository.save(subAdmin);
    }
    
    public void unblockSubAdmin(String id) {
        Administrator subAdmin = getSubAdminById(id);
        subAdmin.setStatus(Administrator.AdminStatus.ACTIVE);
        administratorRepository.save(subAdmin);
    }

    public void deleteSubAdmin(String id) {
        Administrator subAdmin = getSubAdminById(id);
        administratorRepository.delete(subAdmin);
    }
    
    public List<Administrator> getAdminsByCreator(String createdBy) {
        return administratorRepository.findByCreatedBy(createdBy);
    }
    
    public List<Administrator> getAllAdministrators() {
        return administratorRepository.findAll();
    }
    
    public List<Administrator> getActiveAdministrators() {
        return administratorRepository.findActiveAdministrators();
    }
    
    public Administrator getAdministratorByMobile(String mobile) {
        return administratorRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("Administrator not found with mobile: " + mobile));
    }

    // Compatibility methods - delegate to mobile
    public Administrator getAdministratorByUsername(String username) {
        return getAdministratorByMobile(username);
    }

    public Administrator getAdministratorByEmail(String email) {
        return getAdministratorByMobile(email);
    }
    
    public void resetPassword(String id, String newPassword) {
        Administrator admin = getSubAdminById(id);
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        administratorRepository.save(admin);

        // Email sending disabled - new password will be provided manually
        // emailService.sendPasswordResetEmail(admin.getEmail(), admin.getFirstName(), newPassword);
    }
    
    public AdminStatistics getAdminStatistics() {
        Long totalAdmins = administratorRepository.countTotalAdministrators();
        Long masterAdmins = administratorRepository.countByRole(Administrator.AdminRole.MASTER);
        Long subAdmins = administratorRepository.countByRole(Administrator.AdminRole.ADMIN);
        Long activeAdmins = administratorRepository.countActiveAdministrators();
        
        return new AdminStatistics(totalAdmins, masterAdmins, subAdmins, activeAdmins);
    }
    
    private String generateAdminId() {
        Integer maxNumber = administratorRepository.findMaxAdminNumber();
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("ADMIN%03d", nextNumber);
    }

    /**
     * Calculate and update total payments overseen by each admin
     */
    private void updateTotalPaymentsForAllAdmins(List<Administrator> admins) {
        for (Administrator admin : admins) {
            updateTotalPaymentsForAdmin(admin);
        }
    }

    /**
     * Calculate total payments overseen by a specific admin
     */
    private void updateTotalPaymentsForAdmin(Administrator admin) {
        System.out.println("=== Calculating payments for admin: " + admin.getId() + " (mobile: " + admin.getMobile() + ") ===");

        // Find all agents created by this admin using mobile number (since agents store mobile as createdBy)
        List<Agent> adminAgents = agentRepository.findByCreatedBy(admin.getMobile());
        System.out.println("Found " + adminAgents.size() + " agents created by admin mobile " + admin.getMobile());

        // Calculate total amount from completed transactions made by these agents
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Agent agent : adminAgents) {
            System.out.println("Checking agent: " + agent.getId());
            // Use the repository method to get total amount for this agent
            BigDecimal agentTotal = transactionRepository.getTotalAmountByAgent(agent.getId());
            System.out.println("Agent " + agent.getId() + " total: " + agentTotal);
            if (agentTotal != null) {
                totalAmount = totalAmount.add(agentTotal);
            }
        }

        System.out.println("Total amount for admin " + admin.getId() + ": " + totalAmount);

        // Update the admin's total payments field
        admin.setTotalPayments(totalAmount.intValue());

        // Save to database
        administratorRepository.save(admin);
        System.out.println("Updated admin " + admin.getId() + " with total payments: " + totalAmount.intValue());
    }

    /**
     * Manually recalculate total payments for all administrators
     * This can be called to sync existing data
     */
    public void recalculateAllTotalPayments() {
        List<Administrator> allAdmins = administratorRepository.findAll();
        updateTotalPaymentsForAllAdmins(allAdmins);
    }
    

    
    // Statistics DTO
    public static class AdminStatistics {
        private Long totalAdmins;
        private Long masterAdmins;
        private Long subAdmins;
        private Long activeAdmins;
        
        public AdminStatistics(Long totalAdmins, Long masterAdmins, Long subAdmins, Long activeAdmins) {
            this.totalAdmins = totalAdmins;
            this.masterAdmins = masterAdmins;
            this.subAdmins = subAdmins;
            this.activeAdmins = activeAdmins;
        }
        
        public Long getTotalAdmins() { return totalAdmins; }
        public void setTotalAdmins(Long totalAdmins) { this.totalAdmins = totalAdmins; }
        public Long getMasterAdmins() { return masterAdmins; }
        public void setMasterAdmins(Long masterAdmins) { this.masterAdmins = masterAdmins; }
        public Long getSubAdmins() { return subAdmins; }
        public void setSubAdmins(Long subAdmins) { this.subAdmins = subAdmins; }
        public Long getActiveAdmins() { return activeAdmins; }
        public void setActiveAdmins(Long activeAdmins) { this.activeAdmins = activeAdmins; }
    }
}
