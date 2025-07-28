package com.votersystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Async
    public void sendAgentCredentials(String toEmail, String firstName, String username, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Voter System - Agent Account Created");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your agent account has been created successfully in the Voter Registration System.\n\n" +
                "Login Credentials:\n" +
                "Username: %s\n" +
                "Password: %s\n\n" +
                "Please download the mobile app and login with these credentials.\n" +
                "For security reasons, please change your password after first login.\n\n" +
                "Best regards,\n" +
                "Voter System Team",
                firstName, username, password
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            System.out.println("Agent credentials sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + toEmail + " - " + e.getMessage());
        }
    }
    
    @Async
    public void sendSubAdminCredentials(String toEmail, String firstName, String username, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Voter System - Sub Admin Account Created");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your sub-administrator account has been created successfully in the Voter Registration System.\n\n" +
                "Login Credentials:\n" +
                "Username: %s\n" +
                "Password: %s\n\n" +
                "Please login to the admin dashboard using these credentials.\n" +
                "Dashboard URL: [Your Dashboard URL]\n\n" +
                "For security reasons, please change your password after first login.\n\n" +
                "Best regards,\n" +
                "Voter System Team",
                firstName, username, password
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            System.out.println("Sub-admin credentials sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + toEmail + " - " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
        }
    }
    
    @Async
    public void sendPasswordResetEmail(String toEmail, String firstName, String newPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Voter System - Password Reset");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your password has been reset successfully.\n\n" +
                "New Password: %s\n\n" +
                "Please login with your new password and change it immediately for security reasons.\n\n" +
                "Best regards,\n" +
                "Voter System Team",
                firstName, newPassword
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            System.out.println("Password reset email sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to: " + toEmail + " - " + e.getMessage());
        }
    }
    
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName, String userType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Voter Registration System");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Welcome to the Voter Registration System!\n\n" +
                "Your account has been created as a %s.\n" +
                "You should receive your login credentials in a separate email.\n\n" +
                "If you have any questions or need assistance, please contact the system administrator.\n\n" +
                "Best regards,\n" +
                "Voter System Team",
                firstName, userType
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            System.out.println("Welcome email sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to: " + toEmail + " - " + e.getMessage());
        }
    }
}
