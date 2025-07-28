package com.votersystem.service;

import com.votersystem.dto.LoginRequest;
import com.votersystem.dto.LoginResponse;
import com.votersystem.dto.MobileLoginRequest;
import com.votersystem.dto.UpdateProfileRequest;
import com.votersystem.entity.Administrator;
import com.votersystem.entity.Agent;
import com.votersystem.entity.LoginLog;
import com.votersystem.repository.AdministratorRepository;
import com.votersystem.repository.AgentRepository;
import com.votersystem.repository.LoginLogRepository;
import com.votersystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private AdministratorRepository administratorRepository;
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private LoginLogRepository loginLogRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public LoginResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        try {
            // Check if the username is actually a mobile number (for agent mobile login)
            if (loginRequest.getUsername().matches("^[0-9]{10}$") || loginRequest.getUsername().matches("^\\+?[1-9]\\d{1,14}$")) {
                // This looks like a mobile number, try mobile login for agents
                return handleMobileLogin(loginRequest.getUsername(), loginRequest.getPassword(), ipAddress, userAgent);
            }

            // Regular username/password authentication
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Check if user is blocked before proceeding
            checkUserBlockedStatus(username);

            // Get user type and ID
            String userType = userDetailsService.getUserType(username);
            String userId = userDetailsService.getUserId(username);
            
            // Generate JWT token
            String token;
            if ("AGENT".equals(userType)) {
                token = jwtUtil.generateAgentToken(username, userId, "AGENT");
            } else {
                token = jwtUtil.generateAdminToken(username, userId, userType);
            }
            
            // Log successful login
            logLoginAttempt(username, userType, ipAddress, userAgent, LoginLog.LoginStatus.SUCCESS, null);
            
            // Get the actual role for administrators
            String role = userType;
            if (administratorRepository.findByUsername(username).isPresent()) {
                role = administratorRepository.findByUsername(username).get().getRole().name();
            }

            return new LoginResponse(
                token,
                username,
                userId,
                role,
                userType,
                jwtUtil.getExpirationTime()
            );
            
        } catch (DisabledException e) {
            // Log blocked user login attempt
            String userType = determineUserType(loginRequest.getUsername());
            logLoginAttempt(loginRequest.getUsername(), userType, ipAddress, userAgent,
                          LoginLog.LoginStatus.FAILED, "Account blocked");
            throw new RuntimeException("Account has been blocked. Please contact administrator.");
        } catch (BadCredentialsException e) {
            // Log failed login
            String userType = determineUserType(loginRequest.getUsername());
            logLoginAttempt(loginRequest.getUsername(), userType, ipAddress, userAgent,
                          LoginLog.LoginStatus.FAILED, "Invalid credentials");
            throw new RuntimeException("Invalid username or password");
        }
    }
    
    public void logout(String token) {
        // In a more sophisticated implementation, you might want to blacklist the token
        // For now, we'll just log the logout (optional)
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            // Log logout if needed
        }
    }
    
    public Object getCurrentUserInfo(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        
        String username = jwtUtil.extractUsername(token);
        String userType = jwtUtil.extractUserType(token);
        
        if ("AGENT".equals(userType)) {
            Optional<Agent> agentOpt = agentRepository.findByMobile(username);
            if (agentOpt.isPresent()) {
                Agent agent = agentOpt.get();
                Map<String, Object> agentInfo = new HashMap<>();
                agentInfo.put("id", agent.getId());
                agentInfo.put("mobile", agent.getMobile());
                agentInfo.put("firstName", agent.getFirstName());
                agentInfo.put("lastName", agent.getLastName());
                agentInfo.put("status", agent.getStatus());
                agentInfo.put("userType", "AGENT");
                agentInfo.put("interfaceStatus", agent.getInterfaceStatus()); // Add interface status
                agentInfo.put("paymentsToday", agent.getPaymentsToday());
                agentInfo.put("totalPayments", agent.getTotalPayments());
                agentInfo.put("lastLocation", agent.getLastLocation());
                agentInfo.put("latitude", agent.getLatitude());
                agentInfo.put("longitude", agent.getLongitude());
                agentInfo.put("createdBy", agent.getCreatedBy());
                agentInfo.put("createdAt", agent.getCreatedAt());
                return agentInfo;
            }
        } else {
            Optional<Administrator> adminOpt = administratorRepository.findByMobile(username);
            if (adminOpt.isPresent()) {
                Administrator admin = adminOpt.get();
                Map<String, Object> adminInfo = new HashMap<>();
                adminInfo.put("id", admin.getId());
                adminInfo.put("mobile", admin.getMobile());
                adminInfo.put("firstName", admin.getFirstName());
                adminInfo.put("lastName", admin.getLastName());
                adminInfo.put("role", admin.getRole());
                adminInfo.put("status", admin.getStatus());
                adminInfo.put("userType", admin.getRole().name());
                adminInfo.put("totalPayments", admin.getTotalPayments());
                return adminInfo;
            }
        }
        
        throw new RuntimeException("User not found");
    }

    private LoginResponse handleMobileLogin(String mobile, String password, String ipAddress, String userAgent) {
        try {
            // First, try to find administrator by mobile number
            Optional<Administrator> adminOpt = administratorRepository.findByMobile(mobile);
            if (adminOpt.isPresent()) {
                Administrator admin = adminOpt.get();

                // Check if admin is active
                if (admin.getStatus() != Administrator.AdminStatus.ACTIVE) {
                    throw new RuntimeException("Administrator account is not active");
                }

                // Verify password
                if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
                    throw new RuntimeException("Invalid mobile number or password");
                }

                // Generate JWT token for admin
                String token = jwtUtil.generateAdminToken(admin.getMobile(), admin.getId(), admin.getRole().toString());

                // Log successful login (disabled SQL logging)
                // LoginLog loginLog = new LoginLog();
                // loginLog.setUsername(admin.getMobile()); // Use mobile as username
                // loginLog.setUserType(admin.getRole() == Administrator.AdminRole.MASTER ?
                //     LoginLog.UserType.MASTER : LoginLog.UserType.ADMIN);
                // loginLog.setIpAddress(ipAddress);
                // loginLog.setUserAgent(userAgent);
                // loginLog.setStatus(LoginLog.LoginStatus.SUCCESS);
                // loginLogRepository.save(loginLog);

                return new LoginResponse(
                    token,
                    admin.getMobile(),
                    admin.getId(),
                    admin.getRole().toString(),
                    admin.getRole().toString(),
                    jwtUtil.extractExpiration(token).getTime()
                );
            }

            // If not found in administrators, try agents
            Agent agent = agentRepository.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("Invalid mobile number or password"));

            // Check if agent is active
            if (agent.getStatus() != Agent.AgentStatus.ACTIVE) {
                throw new RuntimeException("Agent account is not active");
            }

            // Verify password
            if (!passwordEncoder.matches(password, agent.getPasswordHash())) {
                throw new RuntimeException("Invalid mobile number or password");
            }

            // Generate JWT token for agent
            String token = jwtUtil.generateAgentToken(agent.getMobile(), agent.getId(), "AGENT");

            // Log successful login (disabled SQL logging)
            // LoginLog loginLog = new LoginLog();
            // loginLog.setUsername(agent.getMobile()); // Use mobile as username
            // loginLog.setUserType(LoginLog.UserType.AGENT);
            // loginLog.setIpAddress(ipAddress);
            // loginLog.setUserAgent(userAgent);
            // loginLog.setStatus(LoginLog.LoginStatus.SUCCESS);
            // loginLogRepository.save(loginLog);

            return new LoginResponse(
                token,
                agent.getMobile(),
                agent.getId(),
                "AGENT",
                "AGENT",
                jwtUtil.extractExpiration(token).getTime(),
                agent.getInterfaceStatus()
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid mobile number or password");
        }
    }

    public LoginResponse mobileLogin(MobileLoginRequest mobileLoginRequest, String ipAddress, String userAgent) {
        return handleMobileLogin(mobileLoginRequest.getMobile(), mobileLoginRequest.getPassword(), ipAddress, userAgent);
    }

    public LoginResponse refreshToken(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        
        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        String newToken = jwtUtil.refreshTokenIfNeeded(token, userDetails);
        String userType = userDetailsService.getUserType(username);
        String userId = userDetailsService.getUserId(username);
        
        // Get the actual role for administrators
        String role = userType;
        if (administratorRepository.findByMobile(username).isPresent()) {
            role = administratorRepository.findByMobile(username).get().getRole().name();
        }

        return new LoginResponse(
            newToken,
            username,
            userId,
            role,
            userType,
            jwtUtil.getExpirationTime()
        );
    }
    
    public void changePassword(String token, String currentPassword, String newPassword) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        
        String username = jwtUtil.extractUsername(token);
        String userType = jwtUtil.extractUserType(token);
        
        if ("AGENT".equals(userType)) {
            Optional<Agent> agentOpt = agentRepository.findByMobile(username);
            if (agentOpt.isPresent()) {
                Agent agent = agentOpt.get();
                if (!passwordEncoder.matches(currentPassword, agent.getPasswordHash())) {
                    throw new RuntimeException("Current password is incorrect");
                }
                agent.setPasswordHash(passwordEncoder.encode(newPassword));
                agentRepository.save(agent);
            }
        } else {
            Optional<Administrator> adminOpt = administratorRepository.findByMobile(username);
            if (adminOpt.isPresent()) {
                Administrator admin = adminOpt.get();
                if (!passwordEncoder.matches(currentPassword, admin.getPasswordHash())) {
                    throw new RuntimeException("Current password is incorrect");
                }
                admin.setPasswordHash(passwordEncoder.encode(newPassword));
                administratorRepository.save(admin);
            }
        }
    }
    
    private void logLoginAttempt(String username, String userType, String ipAddress, 
                               String userAgent, LoginLog.LoginStatus status, String failureReason) {
        // Disabled SQL logging - logs are still available in console
        // LoginLog.UserType logUserType;
        // try {
        //     logUserType = LoginLog.UserType.valueOf(userType);
        // } catch (Exception e) {
        //     logUserType = LoginLog.UserType.AGENT; // Default
        // }
        
        // LoginLog loginLog = new LoginLog(username, logUserType, ipAddress, userAgent, status, failureReason);
        // loginLogRepository.save(loginLog);
    }
    
    private String determineUserType(String username) {
        if (administratorRepository.findByMobile(username).isPresent()) {
            return administratorRepository.findByMobile(username).get().getRole().name();
        } else if (agentRepository.findByMobile(username).isPresent()) {
            return "AGENT";
        }
        return "UNKNOWN";
    }

    /**
     * Check if user (admin or agent) is blocked and throw exception if blocked
     */
    private void checkUserBlockedStatus(String username) {
        // Check if it's an administrator
        Optional<Administrator> admin = administratorRepository.findByMobile(username);
        if (admin.isPresent()) {
            if (admin.get().getStatus() == Administrator.AdminStatus.BLOCKED) {
                throw new DisabledException("Account has been blocked by administrator");
            }
            return;
        }

        // Check if it's an agent
        Optional<Agent> agent = agentRepository.findByMobile(username);
        if (agent.isPresent()) {
            if (agent.get().getStatus() == Agent.AgentStatus.BLOCKED) {
                throw new DisabledException("Account has been blocked by administrator");
            }
            return;
        }

        // If user not found in either table, let authentication manager handle it
        throw new BadCredentialsException("User not found");
    }

    /**
     * Update profile for Master Admin
     */
    public LoginResponse updateProfile(String token, UpdateProfileRequest request) {
        try {
            // Validate token and extract username
            if (!jwtUtil.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }

            String currentUsername = jwtUtil.extractUsername(token);
            String userType = jwtUtil.extractUserType(token);

            // Only allow Master Admin to update profile
            if (!"MASTER".equals(userType)) {
                throw new BadCredentialsException("Only Master Admin can update profile");
            }

            // Find the administrator by mobile (which is used as username)
            Optional<Administrator> adminOpt = administratorRepository.findByMobile(currentUsername);
            if (adminOpt.isEmpty()) {
                throw new BadCredentialsException("Administrator not found");
            }

            Administrator admin = adminOpt.get();

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPasswordHash())) {
                throw new BadCredentialsException("Current password is incorrect");
            }

            // Check if at least one field is being updated
            boolean isUpdatingPhoneNumber = request.getNewUsername() != null &&
                                          !request.getNewUsername().trim().isEmpty() &&
                                          !request.getNewUsername().equals(currentUsername);
            boolean isUpdatingPassword = request.getNewPassword() != null &&
                                       !request.getNewPassword().trim().isEmpty();

            if (!isUpdatingPhoneNumber && !isUpdatingPassword) {
                throw new BadCredentialsException("At least one field (phone number or password) must be updated");
            }

            String finalPhoneNumber = currentUsername; // Default to current phone number

            // Update phone number if provided and different
            if (isUpdatingPhoneNumber) {
                // Check if new phone number is already taken
                Optional<Administrator> existingAdmin = administratorRepository.findByMobile(request.getNewUsername());
                if (existingAdmin.isPresent()) {
                    throw new BadCredentialsException("Phone number already exists");
                }
                admin.setMobile(request.getNewUsername());
                finalPhoneNumber = request.getNewUsername();
            }

            // Update password if provided
            if (isUpdatingPassword) {
                admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            }

            // Save changes
            administratorRepository.save(admin);

            // Create UserDetails for token generation
            UserDetails userDetails = User.withUsername(finalPhoneNumber)
                    .password(admin.getPasswordHash())
                    .authorities("ROLE_MASTER")
                    .build();

            // Generate new token with updated phone number
            String newToken = jwtUtil.generateToken(userDetails, Map.of("userType", "MASTER"));

            // Create login response
            LoginResponse response = new LoginResponse();
            response.setToken(newToken);
            response.setUsername(finalPhoneNumber);
            response.setUserId(admin.getId());
            response.setRole("MASTER");
            response.setUserType("MASTER");
            response.setExpiresIn(86400L); // 24 hours

            return response;

        } catch (Exception e) {
            System.err.println("Profile update error: " + e.getMessage());
            throw new RuntimeException("Failed to update profile: " + e.getMessage());
        }
    }
}
