package com.xeno.repository;

import com.xeno.entity.ShopifyEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShopifyEventRepository extends JpaRepository<ShopifyEvent, Long> {
    
    Page<ShopifyEvent> findByTenantId(Long tenantId, Pageable pageable);
    
    Page<ShopifyEvent> findByTenantIdAndEventType(Long tenantId, 
                                                    ShopifyEvent.EventType eventType, 
                                                    Pageable pageable);
    
    @Query("SELECT COUNT(e) FROM ShopifyEvent e WHERE e.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(e) FROM ShopifyEvent e WHERE e.tenantId = :tenantId AND e.eventType = :eventType")
    Long countByTenantIdAndEventType(@Param("tenantId") Long tenantId, 
                                      @Param("eventType") ShopifyEvent.EventType eventType);
    
    @Query("SELECT e.eventType, COUNT(e) FROM ShopifyEvent e WHERE e.tenantId = :tenantId " +
           "GROUP BY e.eventType ORDER BY COUNT(e) DESC")
    List<Object[]> getEventCountsByType(@Param("tenantId") Long tenantId);
    
    @Query("SELECT e FROM ShopifyEvent e WHERE e.tenantId = :tenantId AND e.customerId = :customerId " +
           "ORDER BY e.occurredAt DESC")
    List<ShopifyEvent> findByCustomerId(@Param("tenantId") Long tenantId, 
                                         @Param("customerId") Long customerId);
    
    @Query("SELECT e FROM ShopifyEvent e WHERE e.tenantId = :tenantId " +
           "AND e.eventType = 'CART_ABANDONED' AND e.occurredAt >= :since " +
           "ORDER BY e.occurredAt DESC")
    List<ShopifyEvent> findRecentAbandonedCarts(@Param("tenantId") Long tenantId,
                                                  @Param("since") LocalDateTime since);
    
    @Query("SELECT DATE(e.occurredAt), COUNT(e) FROM ShopifyEvent e " +
           "WHERE e.tenantId = :tenantId AND e.eventType = :eventType " +
           "AND e.occurredAt >= :startDate AND e.occurredAt < :endDate " +
           "GROUP BY DATE(e.occurredAt) ORDER BY DATE(e.occurredAt)")
    List<Object[]> getEventTrend(@Param("tenantId") Long tenantId,
                                  @Param("eventType") ShopifyEvent.EventType eventType,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
}
