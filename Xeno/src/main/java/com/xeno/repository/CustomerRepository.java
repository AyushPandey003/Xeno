package com.xeno.repository;

import com.xeno.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Page<Customer> findByTenantId(Long tenantId, Pageable pageable);
    
    Optional<Customer> findByTenantIdAndShopifyCustomerId(Long tenantId, Long shopifyCustomerId);
    
    Optional<Customer> findByTenantIdAndId(Long tenantId, Long id);
    
    Optional<Customer> findByTenantIdAndEmail(Long tenantId, String email);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId ORDER BY c.totalSpent DESC")
    List<Customer> findTopCustomersBySpend(@Param("tenantId") Long tenantId, Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchByTenantId(@Param("tenantId") Long tenantId, 
                                     @Param("search") String search, 
                                     Pageable pageable);
    
    @Query("SELECT SUM(c.totalSpent) FROM Customer c WHERE c.tenantId = :tenantId")
    BigDecimal getTotalCustomerSpend(@Param("tenantId") Long tenantId);
    
    @Query("SELECT AVG(c.totalSpent) FROM Customer c WHERE c.tenantId = :tenantId")
    BigDecimal getAverageCustomerSpend(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tenantId = :tenantId AND c.createdAt >= :since")
    Long countNewCustomersSince(@Param("tenantId") Long tenantId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tenantId = :tenantId AND c.ordersCount > 1")
    Long countReturningCustomers(@Param("tenantId") Long tenantId);
    
    boolean existsByTenantIdAndShopifyCustomerId(Long tenantId, Long shopifyCustomerId);
}
