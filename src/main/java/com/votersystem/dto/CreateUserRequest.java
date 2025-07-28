package com.votersystem.dto;

import com.votersystem.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class CreateUserRequest {
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 120, message = "Age must be less than 120")
    private Integer age;
    
    private User.Gender gender;

    @NotBlank(message = "Vidhansabha Constituency is required")
    private String vidhansabhaNo; // Assembly Constituency Number

    @NotBlank(message = "Vibhagh Kramank is required")
    @Pattern(regexp = "^\\d+$", message = "Vibhagh Kramank can only contain numbers")
    private String vibhaghKramank; // Division/Section Number
    
    private BigDecimal amount;
    
    // Constructors
    public CreateUserRequest() {}
    
    public CreateUserRequest(String firstName, String lastName, Integer age, User.Gender gender, 
                           String vidhansabhaNo, String vibhaghKramank, BigDecimal amount) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.vidhansabhaNo = vidhansabhaNo;
        this.vibhaghKramank = vibhaghKramank;
        this.amount = amount;
    }
    
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public User.Gender getGender() {
        return gender;
    }
    
    public void setGender(User.Gender gender) {
        this.gender = gender;
    }
    
    public String getVidhansabhaNo() {
        return vidhansabhaNo;
    }
    
    public void setVidhansabhaNo(String vidhansabhaNo) {
        this.vidhansabhaNo = vidhansabhaNo;
    }
    
    public String getVibhaghKramank() {
        return vibhaghKramank;
    }
    
    public void setVibhaghKramank(String vibhaghKramank) {
        this.vibhaghKramank = vibhaghKramank;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
