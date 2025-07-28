package com.votersystem.repository;

import com.votersystem.entity.Vidhansabha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VidhansabhaRepository extends JpaRepository<Vidhansabha, Integer> {
    
    // Find by vidhansabha number
    Optional<Vidhansabha> findByVidhansabhaNo(Integer vidhansabhaNo);
    
    // Find by district name
    List<Vidhansabha> findByDistrictNameIgnoreCase(String districtName);
    
    // Find by assembly name (partial match)
    List<Vidhansabha> findByAssemblyNameContainingIgnoreCase(String assemblyName);
    
    // Search by vidhansabha number or assembly name
    @Query("SELECT v FROM Vidhansabha v WHERE " +
           "CAST(v.vidhansabhaNo AS string) LIKE %:searchTerm% OR " +
           "LOWER(v.assemblyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Vidhansabha> searchByNumberOrName(@Param("searchTerm") String searchTerm);
    
    // Get all districts
    @Query("SELECT DISTINCT v.districtName FROM Vidhansabha v ORDER BY v.districtName")
    List<String> findAllDistricts();
    
    // Get constituencies by category
    List<Vidhansabha> findByCategory(String category);
    
    // Get constituencies by district and category
    List<Vidhansabha> findByDistrictNameIgnoreCaseAndCategory(String districtName, String category);
}
