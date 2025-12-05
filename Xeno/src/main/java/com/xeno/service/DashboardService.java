package com.xeno.service;

import com.xeno.dto.DashboardDto;
import com.xeno.entity.Customer;
import com.xeno.entity.ShopifyEvent;
import com.xeno.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating dashboard analytics and insights.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopifyEventRepository shopifyEventRepository;
    
    /**
     * Get overview statistics for the dashboard
     */
    @Transactional(readOnly = true)
    public DashboardDto.OverviewStats getOverviewStats(Long tenantId) {
        // Current stats
        Long totalCustomers = customerRepository.countByTenantId(tenantId);
        Long totalOrders = orderRepository.countByTenantId(tenantId);
        BigDecimal totalRevenue = orderRepository.getTotalRevenue(tenantId);
        Long totalProducts = productRepository.countByTenantId(tenantId);
        BigDecimal avgOrderValue = orderRepository.getAverageOrderValue(tenantId);
        
        // Handle nulls
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (avgOrderValue == null) avgOrderValue = BigDecimal.ZERO;
        
        // This month stats
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();
        
        Long newCustomersThisMonth = customerRepository.countNewCustomersSince(tenantId, startOfMonth);
        Long ordersThisMonth = orderRepository.countByDateRange(tenantId, startOfMonth, endOfMonth);
        BigDecimal revenueThisMonth = orderRepository.getRevenueByDateRange(tenantId, startOfMonth, endOfMonth);
        if (revenueThisMonth == null) revenueThisMonth = BigDecimal.ZERO;
        
        // Last month stats for comparison
        LocalDateTime startOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = startOfMonth;
        
        Long customersLastMonth = customerRepository.countNewCustomersSince(tenantId, startOfLastMonth);
        Long ordersLastMonth = orderRepository.countByDateRange(tenantId, startOfLastMonth, endOfLastMonth);
        BigDecimal revenueLastMonth = orderRepository.getRevenueByDateRange(tenantId, startOfLastMonth, endOfLastMonth);
        if (revenueLastMonth == null) revenueLastMonth = BigDecimal.ZERO;
        
        // Calculate percentage changes
        Double customersChange = calculatePercentageChange(customersLastMonth, newCustomersThisMonth);
        Double ordersChange = calculatePercentageChange(ordersLastMonth, ordersThisMonth);
        Double revenueChange = calculatePercentageChange(revenueLastMonth, revenueThisMonth);
        
        return DashboardDto.OverviewStats.builder()
                .totalCustomers(totalCustomers)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalProducts(totalProducts)
                .averageOrderValue(avgOrderValue.setScale(2, RoundingMode.HALF_UP))
                .newCustomersThisMonth(newCustomersThisMonth)
                .ordersThisMonth(ordersThisMonth)
                .revenueThisMonth(revenueThisMonth)
                .customersChange(customersChange)
                .ordersChange(ordersChange)
                .revenueChange(revenueChange)
                .build();
    }
    
    /**
     * Get orders by date for chart display
     */
    @Transactional(readOnly = true)
    public List<DashboardDto.OrdersByDate> getOrdersByDate(Long tenantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        List<Object[]> results = orderRepository.getOrdersByDateGrouped(tenantId, start, end);
        
        return results.stream()
                .map(row -> DashboardDto.OrdersByDate.builder()
                        .date(((java.sql.Date) row[0]).toLocalDate().atStartOfDay())
                        .orderCount(((Number) row[1]).longValue())
                        .revenue((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Get top customers by spend
     */
    @Transactional(readOnly = true)
    public List<DashboardDto.TopCustomer> getTopCustomers(Long tenantId, int limit) {
        List<Customer> customers = customerRepository.findTopCustomersBySpend(
                tenantId, 
                PageRequest.of(0, limit)
        );
        
        return customers.stream()
                .map(c -> DashboardDto.TopCustomer.builder()
                        .id(c.getId())
                        .name(c.getFullName())
                        .email(c.getEmail())
                        .totalSpent(c.getTotalSpent())
                        .ordersCount(c.getOrdersCount())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Get revenue trends (monthly)
     */
    @Transactional(readOnly = true)
    public List<DashboardDto.RevenueTrend> getRevenueTrends(Long tenantId, int months) {
        LocalDateTime startDate = LocalDate.now().minusMonths(months).withDayOfMonth(1).atStartOfDay();
        
        List<Object[]> results = orderRepository.getMonthlyTrends(tenantId, startDate);
        
        return results.stream()
                .map(row -> DashboardDto.RevenueTrend.builder()
                        .period((String) row[0])
                        .orderCount(((Number) row[1]).longValue())
                        .revenue((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Get top selling products
     */
    @Transactional(readOnly = true)
    public List<DashboardDto.ProductPerformance> getTopProducts(Long tenantId, int limit) {
        List<Object[]> results = orderItemRepository.getTopSellingProducts(tenantId);
        
        return results.stream()
                .limit(limit)
                .map(row -> DashboardDto.ProductPerformance.builder()
                        .productTitle((String) row[0])
                        .quantitySold(((Number) row[1]).longValue())
                        .revenue((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Get order status breakdown
     */
    @Transactional(readOnly = true)
    public List<DashboardDto.OrderStatusBreakdown> getOrderStatusBreakdown(Long tenantId) {
        List<Object[]> results = orderRepository.getOrderCountByFinancialStatus(tenantId);
        Long totalOrders = orderRepository.countByTenantId(tenantId);
        
        return results.stream()
                .map(row -> {
                    Long count = ((Number) row[1]).longValue();
                    Double percentage = totalOrders > 0 
                            ? (count.doubleValue() / totalOrders) * 100 
                            : 0.0;
                    
                    return DashboardDto.OrderStatusBreakdown.builder()
                            .status(row[0].toString())
                            .count(count)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get event statistics
     */
    @Transactional(readOnly = true)
    public List<DashboardDto.EventStats> getEventStats(Long tenantId) {
        List<Object[]> results = shopifyEventRepository.getEventCountsByType(tenantId);
        
        return results.stream()
                .map(row -> DashboardDto.EventStats.builder()
                        .eventType(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Get complete dashboard data
     */
    @Transactional(readOnly = true)
    public DashboardDto.DashboardData getDashboardData(Long tenantId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        return DashboardDto.DashboardData.builder()
                .overview(getOverviewStats(tenantId))
                .ordersByDate(getOrdersByDate(tenantId, startDate, endDate))
                .topCustomers(getTopCustomers(tenantId, 5))
                .revenueTrends(getRevenueTrends(tenantId, 12))
                .topProducts(getTopProducts(tenantId, 10))
                .orderStatusBreakdown(getOrderStatusBreakdown(tenantId))
                .eventStats(getEventStats(tenantId))
                .build();
    }
    
    private Double calculatePercentageChange(Long previous, Long current) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        return ((current.doubleValue() - previous) / previous) * 100;
    }
    
    private Double calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
