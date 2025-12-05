package com.xeno.repository;

import com.xeno.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByTenantId(Long tenantId);
    
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.active = true")
    List<User> findActiveUsersByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT u FROM User u JOIN FETCH u.tenant WHERE u.email = :email")
    Optional<User> findByEmailWithTenant(@Param("email") String email);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);
}
