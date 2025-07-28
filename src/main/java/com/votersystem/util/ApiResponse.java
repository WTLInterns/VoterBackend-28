package com.votersystem.util;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    private int status;
    
    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, String message, T data, String error, int status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
    
    // Static factory methods for success responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null, 200);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, 200);
    }
    
    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return new ApiResponse<>(true, message, data, null, status);
    }
    
    // Static factory methods for error responses
    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(false, null, null, error, 400);
    }
    
    public static <T> ApiResponse<T> error(String error, int status) {
        return new ApiResponse<>(false, null, null, error, status);
    }
    
    public static <T> ApiResponse<T> error(String message, String error, int status) {
        return new ApiResponse<>(false, message, null, error, status);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
}
