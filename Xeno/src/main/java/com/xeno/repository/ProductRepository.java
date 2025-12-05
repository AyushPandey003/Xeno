package com.xeno.repository;

import com.xeno.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByTenantId(Long tenantId, Pageable pageable);
    
    Optional<Product> findByTenantIdAndShopifyProductId(Long tenantId, Long shopifyProductId);
    
    Optional<Product> findByTenantIdAndId(Long tenantId, Long id);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.vendor) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchByTenantId(@Param("tenantId") Long tenantId, 
                                    @Param("search") String search, 
                                    Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE'")
    Page<Product> findActiveByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.inventoryQuantity < :threshold")
    List<Product> findLowInventory(@Param("tenantId") Long tenantId, @Param("threshold") Integer threshold);
    
    @Query("SELECT DISTINCT p.vendor FROM Product p WHERE p.tenantId = :tenantId AND p.vendor IS NOT NULL")
    List<String> findDistinctVendors(@Param("tenantId") Long tenantId);
    
    @Query("SELECT DISTINCT p.productType FROM Product p WHERE p.tenantId = :tenantId AND p.productType IS NOT NULL")
    List<String> findDistinctProductTypes(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE'")
    Long countActiveByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT SUM(p.inventoryQuantity) FROM Product p WHERE p.tenantId = :tenantId")
    Long getTotalInventory(@Param("tenantId") Long tenantId);
    
    boolean existsByTenantIdAndShopifyProductId(Long tenantId, Long shopifyProductId);
}
