package com.xeno.repository;

import com.xeno.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    Optional<Tenant> findByShopifyDomain(String shopifyDomain);
    
    boolean existsByShopifyDomain(String shopifyDomain);
    
    List<Tenant> findByActiveTrue();
    
    @Query("SELECT t FROM Tenant t WHERE t.active = true AND t.shopifyConnected = true")
    List<Tenant> findAllActiveAndConnected();
    
    @Query("SELECT t FROM Tenant t WHERE t.active = true AND t.shopifyConnected = true AND t.syncStatus != 'IN_PROGRESS'")
    List<Tenant> findTenantsForSync();
}
