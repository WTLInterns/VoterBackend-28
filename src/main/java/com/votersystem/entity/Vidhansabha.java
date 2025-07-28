package com.votersystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "vidhansabha_constituencies")
public class Vidhansabha {
    
    @Id
    @NotNull(message = "Vidhansabha number is required")
    @Column(name = "vidhansabha_no", nullable = false)
    private Integer vidhansabhaNo;
    
    @NotBlank(message = "Assembly name is required")
    @Column(name = "assembly_name", nullable = false, length = 100)
    private String assemblyName;
    
    @NotBlank(message = "District name is required")
    @Column(name = "district_name", nullable = false, length = 50)
    private String districtName;
    
    @Column(name = "category", length = 10)
    private String category; // ST, SC, or null for General
    
    // Constructors
    public Vidhansabha() {}
    
    public Vidhansabha(Integer vidhansabhaNo, String assemblyName, String districtName, String category) {
        this.vidhansabhaNo = vidhansabhaNo;
        this.assemblyName = assemblyName;
        this.districtName = districtName;
        this.category = category;
    }
    
    // Getters and Setters
    public Integer getVidhansabhaNo() {
        return vidhansabhaNo;
    }
    
    public void setVidhansabhaNo(Integer vidhansabhaNo) {
        this.vidhansabhaNo = vidhansabhaNo;
    }
    
    public String getAssemblyName() {
        return assemblyName;
    }
    
    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }
    
    public String getDistrictName() {
        return districtName;
    }
    
    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    @Override
    public String toString() {
        return "Vidhansabha{" +
                "vidhansabhaNo=" + vidhansabhaNo +
                ", assemblyName='" + assemblyName + '\'' +
                ", districtName='" + districtName + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
