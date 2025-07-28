package com.votersystem.controller;

import com.votersystem.dto.UpdateAgentInterfaceRequest;
import com.votersystem.entity.Administrator;
import com.votersystem.entity.Agent;
import com.votersystem.service.AdminService;
import com.votersystem.service.AgentService;
import com.votersystem.util.ApiResponse;
import com.votersystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admins")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Master Admin creates Sub Admin
    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Administrator>> createSubAdmin(
            @Valid @RequestBody CreateSubAdminRequest request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        String masterUsername = jwtUtil.extractUsername(token);
        
        Administrator subAdmin = adminService.createSubAdmin(
            request.getFirstName(),
            request.getLastName(),
            request.getMobile(),
            request.getPassword(),
            masterUsername
        );
        
        return ResponseEntity.ok(ApiResponse.success(subAdmin, "Sub-admin created successfully"));
    }
    
    // Get all sub-admins (Master Admin only)
    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<Administrator>>> getAllSubAdmins() {
        List<Administrator> subAdmins = adminService.getAllSubAdmins();
        return ResponseEntity.ok(ApiResponse.success(subAdmins, "Sub-admins retrieved successfully"));
    }
    
    // Get sub-admin by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Administrator>> getSubAdminById(@PathVariable String id) {
        Administrator subAdmin = adminService.getSubAdminById(id);
        return ResponseEntity.ok(ApiResponse.success(subAdmin, "Sub-admin retrieved successfully"));
    }
    
    // Update sub-admin
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Administrator>> updateSubAdmin(
            @PathVariable String id,
            @Valid @RequestBody UpdateSubAdminRequest request) {
        
        Administrator subAdmin = adminService.updateSubAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success(subAdmin, "Sub-admin updated successfully"));
    }
    
    // Block sub-admin
    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> blockSubAdmin(@PathVariable String id) {
        adminService.blockSubAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sub-admin blocked successfully"));
    }
    
    // Unblock sub-admin
    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> unblockSubAdmin(@PathVariable String id) {
        adminService.unblockSubAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sub-admin unblocked successfully"));
    }

    // Delete sub-admin
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> deleteSubAdmin(@PathVariable String id) {
        adminService.deleteSubAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sub-admin deleted successfully"));
    }

    // Recalculate total payments for all admins
    @PostMapping("/recalculate-payments")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> recalculateTotalPayments() {
        adminService.recalculateAllTotalPayments();
        return ResponseEntity.ok(ApiResponse.success(null, "Total payments recalculated successfully"));
    }

    // Recalculate payment totals for all agents
    @PostMapping("/recalculate-agent-payments")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> recalculateAgentPayments() {
        agentService.recalculateAllAgentPayments();
        return ResponseEntity.ok(ApiResponse.success(null, "Agent payment totals recalculated successfully"));
    }
    
    // Sub-admin creates agent
    @PostMapping("/agents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Agent>> createAgent(
            @Valid @RequestBody CreateAgentRequest request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        String adminUsername = jwtUtil.extractUsername(token);
        
        Agent agent = agentService.createAgent(
            request.getFirstName(),
            request.getLastName(),
            request.getMobile(),
            request.getPassword(),
            adminUsername
        );
        
        return ResponseEntity.ok(ApiResponse.success(agent, "Agent created successfully"));
    }
    
    // Get all agents
    @GetMapping("/agents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<Agent>>> getAllAgents(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        String userType = jwtUtil.extractUserType(token);

        List<Agent> agents;
        if ("MASTER".equals(userType)) {
            // Master admin sees all agents
            agents = agentService.getAllAgents();
        } else {
            // Sub-admin sees only their created agents
            agents = agentService.getAgentsByCreator(username);
        }
        
        return ResponseEntity.ok(ApiResponse.success(agents, "Agents retrieved successfully"));
    }
    
    // Get agent by ID
    @GetMapping("/agents/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Agent>> getAgentById(@PathVariable String id) {
        Agent agent = agentService.getAgentById(id);
        return ResponseEntity.ok(ApiResponse.success(agent, "Agent retrieved successfully"));
    }
    
    // Update agent
    @PutMapping("/agents/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Agent>> updateAgent(
            @PathVariable String id,
            @Valid @RequestBody UpdateAgentRequest request) {

        Agent agent = agentService.updateAgent(id, request.getFirstName(),
                                             request.getLastName(), request.getMobile(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(agent, "Agent updated successfully"));
    }

    // Update agent interface status
    @PutMapping("/agents/{id}/interface-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Agent>> updateAgentInterfaceStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateAgentInterfaceRequest request) {

        Agent agent = agentService.updateAgentInterfaceStatus(id, request.getInterfaceStatusAsInteger());
        return ResponseEntity.ok(ApiResponse.success(agent, "Agent interface status updated successfully"));
    }
    
    // Block agent
    @PostMapping("/agents/{id}/block")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> blockAgent(@PathVariable String id) {
        agentService.blockAgent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Agent blocked successfully"));
    }
    
    // Unblock agent
    @PostMapping("/agents/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> unblockAgent(@PathVariable String id) {
        agentService.unblockAgent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Agent unblocked successfully"));
    }

    // Delete agent
    @DeleteMapping("/agents/{id}")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAgent(@PathVariable String id) {
        agentService.deleteAgent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Agent deleted successfully"));
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    // DTOs
    public static class CreateSubAdminRequest {
        private String firstName;
        private String lastName;
        private String mobile;
        private String password;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class UpdateSubAdminRequest {
        private String firstName;
        private String lastName;
        private String mobile;
        private String password; // Optional - only update if provided

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class CreateAgentRequest {
        private String firstName;
        private String lastName;
        private String mobile;
        private String password;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class UpdateAgentRequest {
        private String firstName;
        private String lastName;
        private String mobile;
        private String password; // Optional - only update if provided

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
