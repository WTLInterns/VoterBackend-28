package com.votersystem.controller;

import com.votersystem.service.FileUploadService;
import com.votersystem.util.ApiResponse;
import com.votersystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private JwtUtil jwtUtil;

    // Download Excel template for voter data
    @GetMapping("/template")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] templateData = fileUploadService.generateExcelTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "voter_data_template.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Upload users from CSV/Excel/PDF file
    @PostMapping("/upload/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<FileUploadResult>> uploadUsers(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
        }

        try {
            // Extract username from JWT token
            String token = extractTokenFromRequest(request);
            String uploadedBy = jwtUtil.extractUsername(token);

            FileUploadResult result = fileUploadService.uploadUsers(file, uploadedBy);
            return ResponseEntity.ok(ApiResponse.success(result, "File uploaded successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }
    
    // Export users to Excel
    @GetMapping("/export/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) Boolean paid) {
        
        try {
            byte[] data = fileUploadService.exportUsers(format, paid);
            
            String filename = "users_export." + format;
            String contentType = getContentType(format);
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .header("Content-Type", contentType)
                    .body(data);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Export transactions to Excel
    @GetMapping("/export/transactions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            byte[] data = fileUploadService.exportTransactions(format, startDate, endDate);

            String filename = "transactions_export." + format;
            String contentType = getContentType(format);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .header("Content-Type", contentType)
                    .body(data);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Export agents to Excel
    @GetMapping("/export/agents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<byte[]> exportAgents(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        try {
            String token = extractTokenFromRequest(request);
            String username = jwtUtil.extractUsername(token);
            String userType = jwtUtil.extractUserType(token);

            byte[] data = fileUploadService.exportAgents(format, status, username, userType);

            String filename = "agents_export." + format;
            String contentType = getContentType(format);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/upload/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<FileUploadHistory>>> getUploadHistory(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String userType = jwtUtil.extractUserType(token);
        String username = jwtUtil.extractUsername(token);

        List<FileUploadHistory> history;
        if ("MASTER".equals(userType)) {
            // Master admin sees all upload history
            history = fileUploadService.getUploadHistory();
        } else {
            // Sub-admin sees only their own upload history
            history = fileUploadService.getUploadHistoryByUser(username);
        }

        return ResponseEntity.ok(ApiResponse.success(history, "Upload history retrieved"));
    }
    
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<FileValidationResult>> validateFile(
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to validate"));
        }
        
        try {
            FileValidationResult result = fileUploadService.validateFile(file);
            return ResponseEntity.ok(ApiResponse.success(result, "File validated successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to validate file: " + e.getMessage()));
        }
    }
    
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "csv":
                return "text/csv";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pdf":
                return "application/pdf";
            default:
                return "application/octet-stream";
        }
    }
    
    // DTOs
    public static class FileUploadResult {
        private String filename;
        private long fileSize;
        private int totalRecords;
        private int successfulRecords;
        private int failedRecords;
        private List<String> errors;
        private String uploadTime;
        
        public FileUploadResult(String filename, long fileSize, int totalRecords, 
                              int successfulRecords, int failedRecords, List<String> errors) {
            this.filename = filename;
            this.fileSize = fileSize;
            this.totalRecords = totalRecords;
            this.successfulRecords = successfulRecords;
            this.failedRecords = failedRecords;
            this.errors = errors;
            this.uploadTime = java.time.LocalDateTime.now().toString();
        }
        
        // Getters and setters
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public int getSuccessfulRecords() { return successfulRecords; }
        public void setSuccessfulRecords(int successfulRecords) { this.successfulRecords = successfulRecords; }
        public int getFailedRecords() { return failedRecords; }
        public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public String getUploadTime() { return uploadTime; }
        public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }
    }
    
    public static class FileValidationResult {
        private boolean isValid;
        private String fileType;
        private int estimatedRecords;
        private List<String> warnings;
        private List<String> requiredColumns;
        private List<String> foundColumns;
        
        public FileValidationResult(boolean isValid, String fileType, int estimatedRecords, 
                                  List<String> warnings, List<String> requiredColumns, List<String> foundColumns) {
            this.isValid = isValid;
            this.fileType = fileType;
            this.estimatedRecords = estimatedRecords;
            this.warnings = warnings;
            this.requiredColumns = requiredColumns;
            this.foundColumns = foundColumns;
        }
        
        // Getters and setters
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public int getEstimatedRecords() { return estimatedRecords; }
        public void setEstimatedRecords(int estimatedRecords) { this.estimatedRecords = estimatedRecords; }
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        public List<String> getRequiredColumns() { return requiredColumns; }
        public void setRequiredColumns(List<String> requiredColumns) { this.requiredColumns = requiredColumns; }
        public List<String> getFoundColumns() { return foundColumns; }
        public void setFoundColumns(List<String> foundColumns) { this.foundColumns = foundColumns; }
    }
    
    public static class FileUploadHistory {
        private String filename;
        private String uploadTime;
        private String uploadedBy;
        private int recordsProcessed;
        private String status;
        
        public FileUploadHistory(String filename, String uploadTime, String uploadedBy, 
                               int recordsProcessed, String status) {
            this.filename = filename;
            this.uploadTime = uploadTime;
            this.uploadedBy = uploadedBy;
            this.recordsProcessed = recordsProcessed;
            this.status = status;
        }
        
        // Getters and setters
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getUploadTime() { return uploadTime; }
        public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }
        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
        public int getRecordsProcessed() { return recordsProcessed; }
        public void setRecordsProcessed(int recordsProcessed) { this.recordsProcessed = recordsProcessed; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // Helper method
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
