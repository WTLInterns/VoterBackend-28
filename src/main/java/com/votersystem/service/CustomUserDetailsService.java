package com.votersystem.service;

import com.votersystem.entity.Administrator;
import com.votersystem.entity.Agent;
import com.votersystem.repository.AdministratorRepository;
import com.votersystem.repository.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private AdministratorRepository administratorRepository;
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // First, try to find in administrators (using mobile as username)
        Optional<Administrator> adminOptional = administratorRepository.findByMobile(username);
        if (adminOptional.isPresent()) {
            Administrator admin = adminOptional.get();
            return User.builder()
                    .username(admin.getMobile()) // Use mobile as username
                    .password(admin.getPasswordHash())
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())))
                    .accountExpired(false)
                    .accountLocked(admin.getStatus() != Administrator.AdminStatus.ACTIVE)
                    .credentialsExpired(false)
                    .disabled(admin.getStatus() != Administrator.AdminStatus.ACTIVE)
                    .build();
        }
        
        // Then, try to find in agents (using mobile as username)
        Optional<Agent> agentOptional = agentRepository.findByMobile(username);
        if (agentOptional.isPresent()) {
            Agent agent = agentOptional.get();
            return User.builder()
                    .username(agent.getMobile()) // Use mobile as username
                    .password(agent.getPasswordHash())
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_AGENT")))
                    .accountExpired(false)
                    .accountLocked(agent.getStatus() != Agent.AgentStatus.ACTIVE)
                    .credentialsExpired(false)
                    .disabled(agent.getStatus() != Agent.AgentStatus.ACTIVE)
                    .build();
        }
        
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
    
    // Helper method to get user type
    public String getUserType(String username) {
        try {
            Optional<Administrator> adminOptional = administratorRepository.findByMobile(username);
            if (adminOptional.isPresent()) {
                Administrator admin = adminOptional.get();
                return admin.getRole().name();
            }

            Optional<Agent> agentOptional = agentRepository.findByMobile(username);
            if (agentOptional.isPresent()) {
                return "AGENT";
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error getting user type for username: " + username + ", Error: " + e.getMessage());
            return null;
        }
    }
    
    // Helper method to get user ID
    public String getUserId(String username) {
        Optional<Administrator> adminOptional = administratorRepository.findByMobile(username);
        if (adminOptional.isPresent()) {
            return adminOptional.get().getId();
        }
        
        Optional<Agent> agentOptional = agentRepository.findByMobile(username);
        if (agentOptional.isPresent()) {
            return agentOptional.get().getId();
        }
        
        return null;
    }
}
