package com.votersystem.controller;

import com.votersystem.entity.Vidhansabha;
import com.votersystem.service.VidhansabhaService;
import com.votersystem.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vidhansabha")
@CrossOrigin(origins = "*")
public class VidhansabhaController {
    
    @Autowired
    private VidhansabhaService vidhansabhaService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Vidhansabha>>> getAllConstituencies() {
        List<Vidhansabha> constituencies = vidhansabhaService.getAllConstituencies();
        return ResponseEntity.ok(ApiResponse.success(constituencies, "Constituencies retrieved successfully"));
    }
    
    @GetMapping("/{vidhansabhaNo}")
    public ResponseEntity<ApiResponse<Vidhansabha>> getByVidhansabhaNo(@PathVariable Integer vidhansabhaNo) {
        Optional<Vidhansabha> constituency = vidhansabhaService.getByVidhansabhaNo(vidhansabhaNo);
        if (constituency.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(constituency.get(), "Constituency found"));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Constituency not found for Vidhansabha No: " + vidhansabhaNo));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Vidhansabha>>> searchConstituencies(@RequestParam String term) {
        List<Vidhansabha> constituencies = vidhansabhaService.searchByNumberOrName(term);
        return ResponseEntity.ok(ApiResponse.success(constituencies, "Search results retrieved"));
    }
    
    @GetMapping("/district/{districtName}")
    public ResponseEntity<ApiResponse<List<Vidhansabha>>> getByDistrict(@PathVariable String districtName) {
        List<Vidhansabha> constituencies = vidhansabhaService.getByDistrict(districtName);
        return ResponseEntity.ok(ApiResponse.success(constituencies, "Constituencies for district retrieved"));
    }
    
    @GetMapping("/districts")
    public ResponseEntity<ApiResponse<List<String>>> getAllDistricts() {
        List<String> districts = vidhansabhaService.getAllDistricts();
        return ResponseEntity.ok(ApiResponse.success(districts, "Districts retrieved successfully"));
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Vidhansabha>>> getByCategory(@PathVariable String category) {
        List<Vidhansabha> constituencies = vidhansabhaService.getByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(constituencies, "Constituencies by category retrieved"));
    }
    
    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<String>> initializeData() {
        vidhansabhaService.initializeData();
        return ResponseEntity.ok(ApiResponse.success("Data initialized", "Vidhansabha data initialized successfully"));
    }
}
