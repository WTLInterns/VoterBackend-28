package com.votersystem.controller;

import com.votersystem.entity.User;
import com.votersystem.service.UserService;
import com.votersystem.util.ApiResponse;
import com.votersystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    // Get all users with pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        String userType = jwtUtil.extractUserType(token);

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;

        if ("MASTER".equals(userType)) {
            // Master admin sees all users
            users = userService.getAllUsers(pageable);
        } else {
            // Sub-admin sees only users created by their agents
            users = userService.getUsersBySubAdmin(username, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    
    // Search users by last name (for mobile app)
    @GetMapping("/search")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<User>>> searchUsersByLastName(
            @RequestParam String lastName) {

        List<User> users = userService.searchUsersByLastName(lastName);
        return ResponseEntity.ok(ApiResponse.success(users, "Users found"));
    }

    // Advanced search users by multiple criteria
    @GetMapping("/search/advanced")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<User>>> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) User.Gender gender,
            @RequestParam(required = false) String vidhansabhaNo,
            @RequestParam(required = false) String vibhaghKramank,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        Page<User> users = userService.searchUsers(firstName, lastName, age, gender,
                                                  vidhansabhaNo, vibhaghKramank, paid,
                                                  page, size, authentication);
        return ResponseEntity.ok(ApiResponse.success(users, "Users found"));
    }

    // Simple search for mobile app (returns List instead of Page)
    @GetMapping("/search/mobile")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<User>>> searchUsersForMobile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) User.Gender gender,
            @RequestParam(required = false) String vidhansabhaNo,
            @RequestParam(required = false) String vibhaghKramank,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication) {

        List<User> users = userService.searchUsersForMobile(firstName, lastName, age, gender,
                                                           vidhansabhaNo, vibhaghKramank, paid,
                                                           limit, authentication);
        return ResponseEntity.ok(ApiResponse.success(users, "Users found"));
    }
    
    // Get user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    // Create new user
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<User>> createUser(
            @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) {

        String token = extractTokenFromRequest(httpRequest);
        String createdBy = jwtUtil.extractUsername(token);

        User user = userService.createUser(request, createdBy);
        return ResponseEntity.ok(ApiResponse.success(user, "User created successfully"));
    }
    
    // Update user
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }
    
    // Delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
    
    // Mark user as paid (for mobile app)
    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<User>> markUserAsPaid(
            @PathVariable Long id,
            @RequestBody MarkAsPaidRequest request,
            HttpServletRequest httpRequest) {

        // Extract agent username from JWT token
        String token = extractTokenFromRequest(httpRequest);
        String agentUsername = jwtUtil.extractUsername(token);

        User user = userService.markUserAsPaid(id, agentUsername, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(user, "User marked as paid successfully"));
    }
    
    // Get users by payment status
    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersByPaymentStatus(
            @RequestParam Boolean paid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getUsersByPaymentStatus(paid, pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    
    // Get user statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<UserStatistics>> getUserStatistics() {
        UserStatistics stats = userService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }
    
    // DTOs
    public static class CreateUserRequest {
        private String firstName;
        private String lastName;
        private Integer age;
        private User.Gender gender;
        private String vidhansabhaNo;
        private String vibhaghKramank;
        private BigDecimal amount;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public User.Gender getGender() { return gender; }
        public void setGender(User.Gender gender) { this.gender = gender; }
        public String getVidhansabhaNo() { return vidhansabhaNo; }
        public void setVidhansabhaNo(String vidhansabhaNo) { this.vidhansabhaNo = vidhansabhaNo; }
        public String getVibhaghKramank() { return vibhaghKramank; }
        public void setVibhaghKramank(String vibhaghKramank) { this.vibhaghKramank = vibhaghKramank; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
    
    public static class UpdateUserRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 120, message = "Age must be less than 120")
        private Integer age;

        private User.Gender gender;

        @NotBlank(message = "Vidhansabha Constituency is required")
        private String vidhansabhaNo;

        @NotBlank(message = "Vibhagh Kramank is required")
        @Pattern(regexp = "^\\d+$", message = "Vibhagh Kramank can only contain numbers")
        private String vibhaghKramank;

        private BigDecimal amount;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public User.Gender getGender() { return gender; }
        public void setGender(User.Gender gender) { this.gender = gender; }
        public String getVidhansabhaNo() { return vidhansabhaNo; }
        public void setVidhansabhaNo(String vidhansabhaNo) { this.vidhansabhaNo = vidhansabhaNo; }
        public String getVibhaghKramank() { return vibhaghKramank; }
        public void setVibhaghKramank(String vibhaghKramank) { this.vibhaghKramank = vibhaghKramank; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
    
    public static class MarkAsPaidRequest {
        private BigDecimal amount;

        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    // Helper method
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    public static class UserStatistics {
        private Long totalUsers;
        private Long paidUsers;
        private Long unpaidUsers;
        private Double totalAmountCollected;
        private Long usersPaidToday;
        
        public UserStatistics(Long totalUsers, Long paidUsers, Long unpaidUsers, 
                            Double totalAmountCollected, Long usersPaidToday) {
            this.totalUsers = totalUsers;
            this.paidUsers = paidUsers;
            this.unpaidUsers = unpaidUsers;
            this.totalAmountCollected = totalAmountCollected;
            this.usersPaidToday = usersPaidToday;
        }
        
        // Getters and setters
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        public Long getPaidUsers() { return paidUsers; }
        public void setPaidUsers(Long paidUsers) { this.paidUsers = paidUsers; }
        public Long getUnpaidUsers() { return unpaidUsers; }
        public void setUnpaidUsers(Long unpaidUsers) { this.unpaidUsers = unpaidUsers; }
        public Double getTotalAmountCollected() { return totalAmountCollected; }
        public void setTotalAmountCollected(Double totalAmountCollected) { this.totalAmountCollected = totalAmountCollected; }
        public Long getUsersPaidToday() { return usersPaidToday; }
        public void setUsersPaidToday(Long usersPaidToday) { this.usersPaidToday = usersPaidToday; }
    }
}
