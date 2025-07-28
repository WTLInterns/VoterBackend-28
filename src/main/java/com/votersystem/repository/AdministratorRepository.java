package com.votersystem.repository;

import com.votersystem.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, String> {
    
    // Find by mobile (used for login)
    Optional<Administrator> findByMobile(String mobile);

    // Check if mobile exists
    boolean existsByMobile(String mobile);

    // Compatibility methods - delegate to mobile methods
    default Optional<Administrator> findByUsername(String username) {
        return findByMobile(username);
    }

    default boolean existsByUsername(String username) {
        return existsByMobile(username);
    }

    default boolean existsByEmail(String email) {
        return false; // Email field removed
    }

    default Optional<Administrator> findByEmail(String email) {
        return Optional.empty(); // Email field removed
    }
    
    // Find by role
    List<Administrator> findByRole(Administrator.AdminRole role);
    
    // Find by status
    List<Administrator> findByStatus(Administrator.AdminStatus status);
    
    // Find master admins
    @Query("SELECT a FROM Administrator a WHERE a.role = 'MASTER'")
    List<Administrator> findMasterAdmins();
    
    // Find sub admins
    @Query("SELECT a FROM Administrator a WHERE a.role = 'ADMIN'")
    List<Administrator> findSubAdmins();
    
    // Find active administrators
    @Query("SELECT a FROM Administrator a WHERE a.status = 'ACTIVE'")
    List<Administrator> findActiveAdministrators();
    
    // Find administrators created by specific master admin
    List<Administrator> findByCreatedBy(String createdBy);
    
    // Count total administrators
    @Query("SELECT COUNT(a) FROM Administrator a")
    Long countTotalAdministrators();
    
    // Count by role
    @Query("SELECT COUNT(a) FROM Administrator a WHERE a.role = :role")
    Long countByRole(Administrator.AdminRole role);
    
    // Count active administrators
    @Query("SELECT COUNT(a) FROM Administrator a WHERE a.status = 'ACTIVE'")
    Long countActiveAdministrators();
    
    // Find next admin ID for auto-generation
    @Query("SELECT MAX(CAST(SUBSTRING(a.id, 6) AS int)) FROM Administrator a WHERE a.id LIKE 'ADMIN%'")
    Integer findMaxAdminNumber();
}
