package com.votersystem.service;

// ✅ REMOVED: CSV imports (CSV support removed as per fix report)
import com.votersystem.controller.FileUploadController;
import com.votersystem.entity.Administrator;
import com.votersystem.entity.Agent;
import com.votersystem.entity.FileUploadHistory;
import com.votersystem.entity.Transaction;
import com.votersystem.entity.User;
import com.votersystem.repository.AdministratorRepository;
import com.votersystem.repository.AgentRepository;
import com.votersystem.repository.FileUploadHistoryRepository;
import com.votersystem.repository.TransactionRepository;
import com.votersystem.repository.UserRepository;
// ✅ REMOVED: PDF imports (PDF support removed as per fix report)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
// ✅ REMOVED: DateTimeFormatter import (not used after CSV removal)
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private FileUploadHistoryRepository fileUploadHistoryRepository;
    
    private final List<String> REQUIRED_USER_COLUMNS = Arrays.asList(
        "firstName", "lastName", "age", "gender"
    );

    private final List<String> OPTIONAL_USER_COLUMNS = Arrays.asList(
        "vidhansabhaNo", "vibhaghKramank"
    );
    
    public FileUploadController.FileUploadResult uploadUsers(MultipartFile file, String uploadedBy) throws Exception {
        logger.info("=== UPLOAD USERS ENDPOINT CALLED ===");
        logger.info("File: {}, Size: {}, Uploaded by: {}", file.getOriginalFilename(), file.getSize(), uploadedBy);

        String filename = file.getOriginalFilename();
        long fileSize = file.getSize();
        List<String> errors = new ArrayList<>();
        int totalRecords = 0;
        int successfulRecords = 0;
        int failedRecords = 0;

        // ✅ FIXED: Only support Excel files (CSV and PDF removed as per fix report)
        if (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls")) {
            // Process Excel file with production-ready features
            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                String[] headers = new String[headerRow.getLastCellNum()];
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    headers[i] = headerRow.getCell(i).getStringCellValue();
                }
                logger.info("Excel headers found: {}", Arrays.toString(headers));
                validateHeaders(headers);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    totalRecords++;
                    Row row = sheet.getRow(i);
                    logger.info("=== PROCESSING ROW {} ===", i + 1);

                    try {
                        // ✅ FIXED: Pass uploadedBy to set createdBy field
                        User user = createUserFromExcelRow(row, headers, uploadedBy);
                        userRepository.save(user);
                        successfulRecords++;
                        logger.info("User saved successfully: {} {}", user.getFirstName(), user.getLastName());
                    } catch (Exception e) {
                        failedRecords++;
                        String errorMsg = "Row " + (i + 1) + ": " + e.getMessage();
                        errors.add(errorMsg);
                        logger.error("Failed to process row {}: {}", i + 1, e.getMessage());
                    }
                }
            }
        } else {
            throw new RuntimeException("Unsupported file format. Please upload Excel files (.xlsx or .xls) only.");
        }

        // Save upload history
        FileUploadHistory.UploadStatus status;
        if (failedRecords == 0) {
            status = FileUploadHistory.UploadStatus.SUCCESS;
        } else if (successfulRecords > 0) {
            status = FileUploadHistory.UploadStatus.PARTIAL_SUCCESS;
        } else {
            status = FileUploadHistory.UploadStatus.FAILED;
        }

        String fileType = getFileType(filename);
        String errorMessage = errors.isEmpty() ? null : String.join("; ", errors);

        FileUploadHistory uploadHistory = new FileUploadHistory(
            filename, fileType, fileSize, uploadedBy,
            totalRecords, successfulRecords, failedRecords,
            status, errorMessage
        );

        fileUploadHistoryRepository.save(uploadHistory);

        return new FileUploadController.FileUploadResult(
            filename, fileSize, totalRecords, successfulRecords, failedRecords, errors
        );
    }
    
    public byte[] exportUsers(String format, Boolean paid) throws Exception {
        List<User> users;
        if (paid != null) {
            users = userRepository.findByPaid(paid);
        } else {
            users = userRepository.findAll();
        }
        
        // ✅ FIXED: Only support Excel export (CSV removed as per fix report)
        if ("xlsx".equalsIgnoreCase(format)) {
            return exportUsersToExcel(users);
        } else {
            throw new RuntimeException("Unsupported export format: " + format + ". Only Excel (.xlsx) is supported.");
        }
    }
    
    public byte[] exportTransactions(String format, String startDate, String endDate) throws Exception {
        List<Transaction> transactions;

        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            transactions = transactionRepository.findByCreatedAtBetween(start, end);
        } else {
            transactions = transactionRepository.findAll();
        }

        // ✅ FIXED: Only support Excel export (CSV removed)
        if ("xlsx".equalsIgnoreCase(format)) {
            return exportTransactionsToExcel(transactions);
        } else {
            throw new RuntimeException("Unsupported export format: " + format + ". Only Excel (.xlsx) is supported.");
        }
    }

    public byte[] exportAgents(String format, String status, String username, String userType) throws Exception {
        List<Agent> agents;

        if ("MASTER".equals(userType)) {
            // Master admin sees all agents
            if (status != null) {
                agents = agentRepository.findByStatus(Agent.AgentStatus.valueOf(status.toUpperCase()));
            } else {
                agents = agentRepository.findAll();
            }
        } else {
            // Sub-admin sees only their created agents
            if (status != null) {
                agents = agentRepository.findByCreatedByAndStatus(username, Agent.AgentStatus.valueOf(status.toUpperCase()));
            } else {
                agents = agentRepository.findByCreatedBy(username);
            }
        }

        // ✅ FIXED: Only support Excel export (CSV removed)
        if ("xlsx".equalsIgnoreCase(format)) {
            return exportAgentsToExcel(agents);
        } else {
            throw new RuntimeException("Unsupported export format: " + format + ". Only Excel (.xlsx) is supported.");
        }
    }
    
    public FileUploadController.FileValidationResult validateFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        List<String> warnings = new ArrayList<>();
        List<String> foundColumns = new ArrayList<>();
        int estimatedRecords = 0;
        boolean isValid = true;
        String fileType = "";

        System.out.println("=== FILE VALIDATION DEBUG ===");
        System.out.println("Filename: " + filename);
        System.out.println("File size: " + file.getSize() + " bytes");
        System.out.println("Required columns: " + REQUIRED_USER_COLUMNS);

        // ✅ FIXED: Only support Excel files (CSV and PDF removed as per fix report)
        if (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls")) {
            fileType = "Excel";
            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                System.out.println("Excel sheet name: " + sheet.getSheetName());
                System.out.println("Total rows: " + sheet.getLastRowNum());

                // Read headers with null check and trim
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String cellValue = cell.getStringCellValue();
                        if (cellValue != null) {
                            foundColumns.add(cellValue.trim());
                        }
                    }
                }

                System.out.println("Found columns: " + foundColumns);
                estimatedRecords = sheet.getLastRowNum();

                // Validate required columns (flexible matching)
                for (String required : REQUIRED_USER_COLUMNS) {
                    boolean isPresent = isColumnPresent(foundColumns, required);
                    System.out.println("Checking column '" + required + "': " + (isPresent ? "FOUND" : "MISSING"));
                    if (!isPresent) {
                        isValid = false;
                        warnings.add("Missing required column: " + required + ". Found columns: " + foundColumns);
                    }
                }
            }
        } else {
            isValid = false;
            warnings.add("Unsupported file format. Please upload Excel files (.xlsx or .xls) only.");
            fileType = "Unknown";
        }

        System.out.println("=== VALIDATION RESULT ===");
        System.out.println("Is Valid: " + isValid);
        System.out.println("Warnings: " + warnings);
        System.out.println("Found Columns: " + foundColumns);
        System.out.println("File Type: " + fileType);
        System.out.println("Estimated Records: " + estimatedRecords);
        System.out.println("========================");

        return new FileUploadController.FileValidationResult(
            isValid, fileType, estimatedRecords, warnings, REQUIRED_USER_COLUMNS, foundColumns
        );
    }
    
    public List<FileUploadController.FileUploadHistory> getUploadHistory() {
        List<FileUploadHistory> historyEntities = fileUploadHistoryRepository.findAllByOrderByUploadTimeDesc();

        return historyEntities.stream().map(entity ->
            new FileUploadController.FileUploadHistory(
                entity.getFilename(),
                entity.getUploadTime().toString(),
                entity.getUploadedBy(),
                entity.getTotalRecords(),
                entity.getStatus().toString()
            )
        ).collect(Collectors.toList());
    }

    public List<FileUploadController.FileUploadHistory> getUploadHistoryByUser(String username) {
        List<FileUploadHistory> historyEntities = fileUploadHistoryRepository.findByUploadedBy(username);

        return historyEntities.stream().map(entity ->
            new FileUploadController.FileUploadHistory(
                entity.getFilename(),
                entity.getUploadTime().toString(),
                entity.getUploadedBy(),
                entity.getTotalRecords(),
                entity.getStatus().toString()
            )
        ).collect(Collectors.toList());
    }
    
    private void validateHeaders(String[] headers) {
        List<String> headerList = Arrays.asList(headers);
        for (String required : REQUIRED_USER_COLUMNS) {
            if (!headerList.contains(required)) {
                throw new RuntimeException("Missing required column: " + required);
            }
        }
    }
    
    // ✅ REMOVED: createUserFromCsvRow method (CSV support removed)
    
    // ✅ FIXED: Added uploadedBy parameter and production-ready data processing
    private User createUserFromExcelRow(Row row, String[] headers, String uploadedBy) {
        User user = new User();

        for (int i = 0; i < headers.length && i < row.getLastCellNum(); i++) {
            String originalHeader = headers[i];
            String header = mapColumnName(originalHeader); // Use flexible mapping
            Cell cell = row.getCell(i);
            String value = getCellValueAsString(cell);

            logger.info("Processing column {}: {} -> {} = '{}'", i, originalHeader, header, value);

            switch (header) {
                case "firstName":
                    String cleanedFirstName = cleanName(value);
                    user.setFirstName(cleanedFirstName);
                    logger.info("Set firstName: {} -> {}", value, cleanedFirstName);
                    break;
                case "lastName":
                    String cleanedLastName = cleanName(value);
                    user.setLastName(cleanedLastName);
                    logger.info("Set lastName: {} -> {}", value, cleanedLastName);
                    break;
                case "age":
                    Integer age = parseAge(value);
                    user.setAge(age);
                    logger.info("Set age: {} -> {}", value, age);
                    break;
                case "gender":
                    User.Gender gender = parseGender(value);
                    user.setGender(gender);
                    logger.info("Set gender: {} -> {}", value, gender);
                    break;
                case "vidhansabhaNo":
                    String cleanedVidhansabha = cleanAlphanumeric(value, "vidhansabhaNo");
                    user.setVidhansabhaNo(cleanedVidhansabha);
                    logger.info("Set vidhansabhaNo: {} -> {}", value, cleanedVidhansabha);
                    break;
                case "vibhaghKramank":
                    String cleanedVibhagh = cleanAlphanumeric(value, "vibhaghKramank");
                    user.setVibhaghKramank(cleanedVibhagh);
                    logger.info("Set vibhaghKramank: {} -> {}", value, cleanedVibhagh);
                    break;
                case "amount":
                    // Ignore amount from import, always set to 0
                    // Amount will be set when agent distributes money
                    break;
            }
        }

        // ✅ CRITICAL FIX: Set required fields as per fix report
        user.setAmount(BigDecimal.ZERO);
        user.setPaid(false);
        user.setCreatedBy(uploadedBy); // Critical fix!

        logger.info("User created: {} {}", user.getFirstName(), user.getLastName());
        logger.info("Created by: {}", uploadedBy);

        validateUser(user);
        return user;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    private void validateUser(User user) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }
        if (user.getAge() == null || user.getAge() < 18 || user.getAge() > 120) {
            throw new RuntimeException("Age is required and must be between 18 and 120");
        }
        if (user.getGender() == null) {
            throw new RuntimeException("Gender is required");
        }
    }
    
    // ✅ REMOVED: exportUsersToCSV method (CSV support removed)
    
    private byte[] exportUsersToExcel(List<User> users) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "First Name", "Last Name", "Age", "Gender", "Vidhansabha No", "Vibhagh Kramank", "Distribution Status", "Amount Distributed", "Distribution Date", "Distributed By", "Created By"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create data rows
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getFirstName());
                row.createCell(2).setCellValue(user.getLastName());
                row.createCell(3).setCellValue(user.getAge() != null ? user.getAge() : 0);
                row.createCell(4).setCellValue(user.getGender() != null ? user.getGender().toString() : "");
                row.createCell(5).setCellValue(user.getVidhansabhaNo() != null ? user.getVidhansabhaNo() : "");
                row.createCell(6).setCellValue(user.getVibhaghKramank() != null ? user.getVibhaghKramank() : "");
                row.createCell(7).setCellValue(user.getPaid() ? "DISTRIBUTED" : "PENDING");
                row.createCell(8).setCellValue(user.getAmount() != null ? user.getAmount().doubleValue() : 0);
                row.createCell(9).setCellValue(user.getPaidDate() != null ? user.getPaidDate().toString() : "");

                // Get agent name for "Distributed By" column
                String distributedBy = "";
                if (user.getPaidBy() != null) {
                    Agent agent = agentRepository.findById(user.getPaidBy()).orElse(null);
                    if (agent == null) {
                        agent = agentRepository.findByMobile(user.getPaidBy()).orElse(null);
                    }
                    distributedBy = agent != null ? agent.getFirstName() + " " + agent.getLastName() : user.getPaidBy();
                }
                row.createCell(10).setCellValue(distributedBy);

                // Get admin name for "Created By" column
                String createdBy = "System";
                if (user.getCreatedBy() != null) {
                    Administrator admin = administratorRepository.findByMobile(user.getCreatedBy()).orElse(null);
                    createdBy = admin != null ? admin.getFirstName() + " " + admin.getLastName() : user.getCreatedBy();
                }
                row.createCell(11).setCellValue(createdBy);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    // ✅ REMOVED: CSV export methods (CSV support removed as per fix report)
    
    private byte[] exportTransactionsToExcel(List<Transaction> transactions) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Transaction ID", "User Name", "Agent Name", "Amount", "Location", "Status", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transaction.getId());

                // Get user name
                String userName = "User " + transaction.getUserId();
                User user = userRepository.findById(transaction.getUserId()).orElse(null);
                if (user != null) {
                    userName = user.getFirstName() + " " + user.getLastName();
                }
                row.createCell(1).setCellValue(userName);

                // Get agent name
                String agentName = transaction.getAgentId();
                Agent agent = agentRepository.findById(transaction.getAgentId()).orElse(null);
                if (agent != null) {
                    agentName = agent.getFirstName() + " " + agent.getLastName();
                }
                row.createCell(2).setCellValue(agentName);

                row.createCell(3).setCellValue(transaction.getAmount().doubleValue());
                row.createCell(4).setCellValue(transaction.getLocation() != null ? transaction.getLocation() : "");
                row.createCell(5).setCellValue(transaction.getStatus().toString());
                row.createCell(6).setCellValue(transaction.getCreatedAt().toString());
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ✅ REMOVED: parsePdfHeaderLine method (PDF support removed)

    // ✅ REMOVED: createUserFromPdfLine method (PDF support removed)

    /**
     * Flexible column matching for production use
     * Handles case insensitivity, spaces, underscores, and common variations
     */
    private boolean isColumnPresent(List<String> foundColumns, String requiredColumn) {
        String normalizedRequired = normalizeColumnName(requiredColumn);

        return foundColumns.stream()
            .anyMatch(col -> {
                String normalizedFound = normalizeColumnName(col);
                return normalizedFound.equals(normalizedRequired) ||
                       isColumnVariation(normalizedFound, normalizedRequired);
            });
    }

    private String normalizeColumnName(String columnName) {
        if (columnName == null) return "";
        return columnName.toLowerCase()
                        .trim()
                        .replaceAll("[\\s_-]+", "")
                        .replaceAll("[^a-z0-9]", "");
    }

    private boolean isColumnVariation(String found, String required) {
        // Handle common variations
        Map<String, List<String>> variations = new HashMap<>();
        variations.put("firstname", Arrays.asList("fname", "first", "name"));
        variations.put("lastname", Arrays.asList("lname", "last", "surname"));
        variations.put("age", Arrays.asList("years", "yrs"));
        variations.put("gender", Arrays.asList("sex", "malefemale"));
        variations.put("vidhansabhano", Arrays.asList("constituency", "assembly", "vidhan", "sabha"));
        variations.put("vibhaghkramank", Arrays.asList("division", "section", "vibhag", "kramank"));

        return variations.getOrDefault(required, new ArrayList<>()).contains(found) ||
               variations.getOrDefault(found, new ArrayList<>()).contains(required);
    }

    /**
     * Maps found column names to standard field names
     */
    private String mapColumnName(String columnName) {
        String normalized = normalizeColumnName(columnName);

        // Direct mapping
        if (normalized.equals("firstname") || normalized.contains("first") || normalized.equals("fname")) {
            return "firstName";
        } else if (normalized.equals("lastname") || normalized.contains("last") || normalized.equals("lname") || normalized.contains("surname")) {
            return "lastName";
        } else if (normalized.equals("age") || normalized.equals("years") || normalized.equals("yrs")) {
            return "age";
        } else if (normalized.equals("gender") || normalized.equals("sex")) {
            return "gender";
        } else if (normalized.contains("vidhansabha") || normalized.contains("constituency") || normalized.contains("assembly")) {
            return "vidhansabhaNo";
        } else if (normalized.contains("vibhagh") || normalized.contains("kramank") || normalized.contains("division") || normalized.contains("section")) {
            return "vibhaghKramank";
        }

        return columnName; // Return original if no mapping found
    }

    private String getFileType(String filename) {
        if (filename.toLowerCase().endsWith(".csv")) {
            return "CSV";
        } else if (filename.toLowerCase().endsWith(".xlsx")) {
            return "Excel";
        } else if (filename.toLowerCase().endsWith(".pdf")) {
            return "PDF";
        } else {
            return "Unknown";
        }
    }

    // ✅ REMOVED: exportAgentsToCSV method (CSV support removed as per fix report)

    private byte[] exportAgentsToExcel(List<Agent> agents) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Agents");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Agent ID", "First Name", "Last Name", "Mobile", "Status", "Created By", "Today's Distribution", "Total Distribution", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Write data rows
            int rowNum = 1;
            for (Agent agent : agents) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(agent.getId());
                row.createCell(1).setCellValue(agent.getFirstName());
                row.createCell(2).setCellValue(agent.getLastName());
                row.createCell(3).setCellValue(agent.getMobile());
                row.createCell(4).setCellValue(agent.getStatus().toString());

                // Get admin name for "Created By" column
                String createdBy = "Master";
                if (agent.getCreatedBy() != null) {
                    Administrator admin = administratorRepository.findByMobile(agent.getCreatedBy()).orElse(null);
                    createdBy = admin != null ? admin.getFirstName() + " " + admin.getLastName() : agent.getCreatedBy();
                }
                row.createCell(5).setCellValue(createdBy);

                row.createCell(6).setCellValue(agent.getPaymentsToday()); // Today's Distribution
                row.createCell(7).setCellValue(agent.getTotalPayments()); // Total Distribution
                row.createCell(8).setCellValue(agent.getCreatedAt() != null ? agent.getCreatedAt().toString() : "");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generateExcelTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Voter Data Template");

            // Create header row with correct field names
            Row headerRow = sheet.createRow(0);
            String[] headers = {"firstName", "lastName", "age", "gender", "vidhansabhaNo", "vibhaghKramank"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // Make header bold
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Add sample data rows
            String[][] sampleData = {
                {"Rajesh", "Sharma", "35", "MALE", "288", "01"},
                {"Priya", "Patel", "28", "FEMALE", "289", "02"},
                {"Amit", "Kumar", "42", "MALE", "290", "03"},
                {"Sunita", "Singh", "31", "FEMALE", "288", "04"},
                {"Vikram", "Gupta", "38", "MALE", "291", "01"}
            };

            for (int i = 0; i < sampleData.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < sampleData[i].length; j++) {
                    row.createCell(j).setCellValue(sampleData[i][j]);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ✅ PRODUCTION-READY HELPER METHODS (as per fix report)

    /**
     * Clean and standardize name fields
     */
    private String cleanName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        // Remove extra spaces, special characters, and capitalize properly
        String cleaned = name.trim()
                .replaceAll("[^a-zA-Z\\s]", "") // Remove special characters
                .replaceAll("\\s+", " "); // Replace multiple spaces with single space

        // Capitalize first letter of each word
        String[] words = cleaned.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }

        String finalName = result.toString().trim();
        if (finalName.length() < 2 || finalName.length() > 50) {
            throw new RuntimeException("Name must be between 2 and 50 characters: " + finalName);
        }

        return finalName;
    }

    /**
     * Parse age with decimal support (handles 25.0 -> 25)
     */
    private Integer parseAge(String ageStr) {
        if (ageStr == null || ageStr.trim().isEmpty()) {
            throw new RuntimeException("Age cannot be empty");
        }

        try {
            // Handle decimal values like 25.0
            double ageDouble = Double.parseDouble(ageStr.trim());
            int age = (int) ageDouble;

            // Business rule: voting age requirement
            if (age < 18 || age > 120) {
                throw new RuntimeException("Age must be between 18 and 120 years: " + age);
            }

            return age;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid age format: " + ageStr);
        }
    }

    /**
     * Parse and standardize gender values
     */
    private User.Gender parseGender(String genderStr) {
        if (genderStr == null || genderStr.trim().isEmpty()) {
            throw new RuntimeException("Gender cannot be empty");
        }

        String gender = genderStr.trim().toUpperCase();

        // Handle multiple gender formats
        switch (gender) {
            case "M":
            case "MALE":
            case "1":
                return User.Gender.MALE;
            case "F":
            case "FEMALE":
            case "2":
                return User.Gender.FEMALE;
            case "O":
            case "OTHER":
            case "3":
                return User.Gender.OTHER;
            default:
                throw new RuntimeException("Invalid gender: " + genderStr + ". Use M/Male/1, F/Female/2, or O/Other/3");
        }
    }

    /**
     * Clean alphanumeric fields (vidhansabhaNo, vibhaghKramank)
     */
    private String cleanAlphanumeric(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            // These fields are optional
            return null;
        }

        // Remove special characters except numbers and letters
        String cleaned = value.trim().replaceAll("[^a-zA-Z0-9]", "");

        if (cleaned.length() > 20) {
            throw new RuntimeException(fieldName + " too long (max 20 characters): " + cleaned);
        }

        return cleaned.isEmpty() ? null : cleaned;
    }
}
