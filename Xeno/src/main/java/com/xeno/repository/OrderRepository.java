package com.xeno.repository;

import com.xeno.entity.Order;
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
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Page<Order> findByTenantId(Long tenantId, Pageable pageable);
    
    Optional<Order> findByTenantIdAndShopifyOrderId(Long tenantId, Long shopifyOrderId);
    
    Optional<Order> findByTenantIdAndId(Long tenantId, Long id);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.tenantId = :tenantId")
    BigDecimal getTotalRevenue(@Param("tenantId") Long tenantId);
    
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.tenantId = :tenantId " +
           "AND o.processedAt >= :startDate AND o.processedAt < :endDate")
    BigDecimal getRevenueByDateRange(@Param("tenantId") Long tenantId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId " +
           "AND o.processedAt >= :startDate AND o.processedAt < :endDate " +
           "ORDER BY o.processedAt DESC")
    Page<Order> findByDateRange(@Param("tenantId") Long tenantId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = :tenantId " +
           "AND o.processedAt >= :startDate AND o.processedAt < :endDate")
    Long countByDateRange(@Param("tenantId") Long tenantId,
                          @Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT DATE(o.processedAt) as orderDate, COUNT(o) as orderCount, SUM(o.totalPrice) as revenue " +
           "FROM Order o WHERE o.tenantId = :tenantId " +
           "AND o.processedAt >= :startDate AND o.processedAt < :endDate " +
           "GROUP BY DATE(o.processedAt) ORDER BY orderDate")
    List<Object[]> getOrdersByDateGrouped(@Param("tenantId") Long tenantId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(o.totalPrice) FROM Order o WHERE o.tenantId = :tenantId")
    BigDecimal getAverageOrderValue(@Param("tenantId") Long tenantId);
    
    @Query("SELECT o.financialStatus, COUNT(o) FROM Order o WHERE o.tenantId = :tenantId " +
           "GROUP BY o.financialStatus")
    List<Object[]> getOrderCountByFinancialStatus(@Param("tenantId") Long tenantId);
    
    @Query("SELECT o.fulfillmentStatus, COUNT(o) FROM Order o WHERE o.tenantId = :tenantId " +
           "AND o.fulfillmentStatus IS NOT NULL GROUP BY o.fulfillmentStatus")
    List<Object[]> getOrderCountByFulfillmentStatus(@Param("tenantId") Long tenantId);
    
    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId ORDER BY o.totalPrice DESC")
    List<Order> findTopOrdersByValue(@Param("tenantId") Long tenantId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.customerId = :customerId " +
           "ORDER BY o.processedAt DESC")
    List<Order> findByCustomerId(@Param("tenantId") Long tenantId, 
                                  @Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = :tenantId AND o.cancelled = true")
    Long countCancelledOrders(@Param("tenantId") Long tenantId);
    
    @Query("SELECT SUM(o.totalDiscount) FROM Order o WHERE o.tenantId = :tenantId")
    BigDecimal getTotalDiscounts(@Param("tenantId") Long tenantId);
    
    boolean existsByTenantIdAndShopifyOrderId(Long tenantId, Long shopifyOrderId);
    
    // Monthly trends
    @Query(value = "SELECT TO_CHAR(o.processed_at, 'YYYY-MM') as month, " +
                   "COUNT(*) as orderCount, SUM(o.total_price) as revenue " +
                   "FROM orders o WHERE o.tenant_id = :tenantId " +
                   "AND o.processed_at >= :startDate " +
                   "GROUP BY TO_CHAR(o.processed_at, 'YYYY-MM') " +
                   "ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyTrends(@Param("tenantId") Long tenantId,
                                    @Param("startDate") LocalDateTime startDate);
}
