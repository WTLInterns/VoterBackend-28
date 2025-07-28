package com.votersystem.config;

import com.votersystem.entity.Administrator;
import com.votersystem.entity.Agent;
import com.votersystem.repository.AdministratorRepository;
import com.votersystem.repository.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeMasterAdmin();
        initializeDemoSubAdmin();
        initializeDemoAgents();
    }
    
    private void initializeMasterAdmin() {
        // Check if master admin already exists
        if (administratorRepository.findByMobile("9999999999").isEmpty()) {
            String password = "master123";

            Administrator masterAdmin = new Administrator(
                "MASTER001",
                "Master",
                "Administrator",
                "9999999999", // Mobile number
                passwordEncoder.encode(password),
                Administrator.AdminRole.MASTER,
                null // No creator for master admin
            );

            administratorRepository.save(masterAdmin);
            System.out.println("Master admin created successfully!");
            System.out.println("Mobile: 9999999999");
            System.out.println("Password: " + password);
        }
    }

    private void initializeDemoSubAdmin() {
        // Check if demo sub-admin already exists
        if (administratorRepository.findByMobile("9888888888").isEmpty()) {
            String password = "admin123";

            Administrator subAdmin = new Administrator(
                "ADMIN001",
                "Demo",
                "Administrator",
                "9888888888", // Mobile number
                passwordEncoder.encode(password),
                Administrator.AdminRole.ADMIN,
                "9999999999" // Created by master admin (mobile)
            );

            administratorRepository.save(subAdmin);
            System.out.println("Demo sub-admin created successfully!");
            System.out.println("Mobile: 9888888888");
            System.out.println("Password: " + password);
        }
    }

    private void initializeDemoAgents() {
        // Check if demo agents already exist
        if (agentRepository.findByMobile("+919876543210").isEmpty()) {
            String password = "agent123";

            Agent agent1 = new Agent(
                "AGENT001",
                "राज",
                "पाटील",
                "+919876543210",
                passwordEncoder.encode(password),
                "9888888888" // Created by demo sub-admin (mobile)
            );

            agentRepository.save(agent1);
            System.out.println("Demo agent 1 created successfully!");
            System.out.println("Mobile: +919876543210");
            System.out.println("Password: " + password);
        }

        if (agentRepository.findByMobile("+919876543211").isEmpty()) {
            String password = "agent123";

            Agent agent2 = new Agent(
                "AGENT002",
                "सुनीता",
                "शर्मा",
                "+919876543211",
                passwordEncoder.encode(password),
                "9888888888" // Created by demo sub-admin (mobile)
            );

            agentRepository.save(agent2);
            System.out.println("Demo agent 2 created successfully!");
            System.out.println("Mobile: +919876543211");
            System.out.println("Password: " + password);
        }

        if (agentRepository.findByMobile("+919876543212").isEmpty()) {
            String password = "agent123";

            Agent agent3 = new Agent(
                "AGENT003",
                "अमित",
                "कुमार",
                "+919876543212",
                passwordEncoder.encode(password),
                "9888888888" // Created by demo sub-admin (mobile)
            );

            agentRepository.save(agent3);
            System.out.println("Demo agent 3 created successfully!");
            System.out.println("Mobile: +919876543212");
            System.out.println("Password: " + password);
        }
    }
}
