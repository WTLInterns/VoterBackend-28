package com.votersystem.exception;

import com.votersystem.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: BadCredentialsException - " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: AuthenticationException - " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: AccessDeniedException - " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied - insufficient permissions", HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: MaxUploadSizeExceededException - " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("File too large - maximum size is 10MB", HttpStatus.PAYLOAD_TOO_LARGE.value()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: DataIntegrityViolationException - " + ex.getMessage());
        
        String message = "Data integrity violation";
        if (ex.getMessage().contains("Duplicate entry")) {
            if (ex.getMessage().contains("email")) {
                message = "Email address already exists";
            } else if (ex.getMessage().contains("username")) {
                message = "Username already exists";
            } else if (ex.getMessage().contains("mobile")) {
                message = "Mobile number already exists";
            } else {
                message = "Duplicate entry - this record already exists";
            }
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message, HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: MethodArgumentNotValidException - " + ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors.toString(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: IllegalArgumentException - " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage() != null ? ex.getMessage() : "Invalid request", 
                      HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: RuntimeException - " + ex.getMessage());
        
        // Don't expose internal error details in production
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "An error occurred while processing your request";
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        System.out.println("GlobalExceptionHandler: Exception - " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        ex.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later.", 
                      HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
