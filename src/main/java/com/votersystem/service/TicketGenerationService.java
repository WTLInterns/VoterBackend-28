package com.votersystem.service;

import com.votersystem.repository.IssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * Service for generating unique issue ticket numbers
 * Format: ISS-YYYY-NNNNNN (e.g., ISS-2025-001234)
 */
@Service
public class TicketGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketGenerationService.class);
    
    @Autowired
    private IssueRepository issueRepository;
    
    private static final String TICKET_PREFIX = "ISS";
    private static final int SEQUENCE_LENGTH = 6;
    
    /**
     * Generate next unique ticket number for current year
     * @return Unique ticket number in format ISS-YYYY-NNNNNN
     */
    @Transactional
    public synchronized String generateTicketNumber() {
        try {
            String currentYear = String.valueOf(Year.now().getValue());
            
            // Get next sequence number for current year
            Integer nextSequence = issueRepository.getNextSequenceNumber(currentYear);
            if (nextSequence == null) {
                nextSequence = 1;
            }
            
            // Format sequence number with leading zeros
            String sequenceStr = String.format("%0" + SEQUENCE_LENGTH + "d", nextSequence);
            
            // Generate ticket number
            String ticketNumber = String.format("%s-%s-%s", TICKET_PREFIX, currentYear, sequenceStr);
            
            logger.info("Generated ticket number: {}", ticketNumber);
            return ticketNumber;
            
        } catch (Exception e) {
            logger.error("Failed to generate ticket number: {}", e.getMessage());
            throw new RuntimeException("Failed to generate ticket number", e);
        }
    }
    
    /**
     * Validate ticket number format
     * @param ticketNumber Ticket number to validate
     * @return true if valid format
     */
    public boolean isValidTicketFormat(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.trim().isEmpty()) {
            return false;
        }
        
        // Check format: ISS-YYYY-NNNNNN
        String pattern = "^ISS-\\d{4}-\\d{6}$";
        return ticketNumber.matches(pattern);
    }
    
    /**
     * Extract year from ticket number
     * @param ticketNumber Ticket number
     * @return Year from ticket number
     */
    public String extractYearFromTicket(String ticketNumber) {
        if (!isValidTicketFormat(ticketNumber)) {
            throw new IllegalArgumentException("Invalid ticket format");
        }
        
        String[] parts = ticketNumber.split("-");
        return parts[1]; // Year part
    }
    
    /**
     * Extract sequence number from ticket number
     * @param ticketNumber Ticket number
     * @return Sequence number from ticket number
     */
    public String extractSequenceFromTicket(String ticketNumber) {
        if (!isValidTicketFormat(ticketNumber)) {
            throw new IllegalArgumentException("Invalid ticket format");
        }
        
        String[] parts = ticketNumber.split("-");
        return parts[2]; // Sequence part
    }
}
