package com.votersystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;
    
    @Column(name = "paid", nullable = false)
    private Boolean paid = false;
    
    @Column(name = "paid_date")
    private LocalDateTime paidDate;
    
    @Column(name = "paid_by", length = 50)
    private String paidBy;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "vidhansabha_no", length = 10)
    private String vidhansabhaNo; // Assembly Constituency Number

    @Column(name = "vibhagh_kramank", length = 10)
    private String vibhaghKramank; // Division/Section Number

    @NotNull(message = "Amount is required")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public User() {}

    public User(String firstName, String lastName, Integer age, Gender gender, BigDecimal amount) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.amount = amount;
    }

    public User(String firstName, String lastName, Integer age, Gender gender,
                String vidhansabhaNo, String vibhaghKramank, BigDecimal amount) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.vidhansabhaNo = vidhansabhaNo;
        this.vibhaghKramank = vibhaghKramank;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public Boolean getPaid() {
        return paid;
    }
    
    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
    
    public LocalDateTime getPaidDate() {
        return paidDate;
    }
    
    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }
    
    public String getPaidBy() {
        return paidBy;
    }
    
    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public void markAsPaid(String agentUsername, BigDecimal paidAmount) {
        this.paid = true;
        this.paidDate = LocalDateTime.now();
        this.paidBy = agentUsername;
        this.amount = paidAmount;
    }

    // Gender Enum
    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
