package com.xeno.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewStats {
        private Long totalCustomers;
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private Long totalProducts;
        private BigDecimal averageOrderValue;
        private Long newCustomersThisMonth;
        private Long ordersThisMonth;
        private BigDecimal revenueThisMonth;
        
        // Percentage changes
        private Double customersChange;
        private Double ordersChange;
        private Double revenueChange;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdersByDate {
        private LocalDateTime date;
        private Long orderCount;
        private BigDecimal revenue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private Long id;
        private String name;
        private String email;
        private BigDecimal totalSpent;
        private Integer ordersCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueTrend {
        private String period;
        private BigDecimal revenue;
        private Long orderCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductPerformance {
        private String productTitle;
        private Long quantitySold;
        private BigDecimal revenue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusBreakdown {
        private String status;
        private Long count;
        private Double percentage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventStats {
        private String eventType;
        private Long count;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardData {
        private OverviewStats overview;
        private List<OrdersByDate> ordersByDate;
        private List<TopCustomer> topCustomers;
        private List<RevenueTrend> revenueTrends;
        private List<ProductPerformance> topProducts;
        private List<OrderStatusBreakdown> orderStatusBreakdown;
        private List<EventStats> eventStats;
    }
}
