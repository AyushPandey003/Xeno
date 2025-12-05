package com.xeno.repository;

import com.xeno.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    @Query("SELECT oi.productTitle, SUM(oi.quantity) as totalQuantity, SUM(oi.price * oi.quantity) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o WHERE o.tenantId = :tenantId " +
           "GROUP BY oi.productTitle ORDER BY totalRevenue DESC")
    List<Object[]> getTopSellingProducts(@Param("tenantId") Long tenantId);
    
    @Query("SELECT oi.productTitle, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi JOIN oi.order o WHERE o.tenantId = :tenantId " +
           "GROUP BY oi.productTitle ORDER BY totalQuantity DESC")
    List<Object[]> getProductsByQuantitySold(@Param("tenantId") Long tenantId);
    
    void deleteByOrderId(Long orderId);
}
