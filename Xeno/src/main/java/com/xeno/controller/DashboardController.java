package com.xeno.controller;

import com.xeno.dto.ApiResponse;
import com.xeno.dto.DashboardDto;
import com.xeno.security.TenantContext;
import com.xeno.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard controller for analytics and insights.
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics and insights dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping
    @Operation(summary = "Get complete dashboard data")
    public ResponseEntity<ApiResponse<DashboardDto.DashboardData>> getDashboard() {
        Long tenantId = TenantContext.getTenantId();
        DashboardDto.DashboardData data = dashboardService.getDashboardData(tenantId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get overview statistics")
    public ResponseEntity<ApiResponse<DashboardDto.OverviewStats>> getOverviewStats() {
        Long tenantId = TenantContext.getTenantId();
        DashboardDto.OverviewStats stats = dashboardService.getOverviewStats(tenantId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/orders-by-date")
    @Operation(summary = "Get orders grouped by date")
    public ResponseEntity<ApiResponse<List<DashboardDto.OrdersByDate>>> getOrdersByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long tenantId = TenantContext.getTenantId();
        List<DashboardDto.OrdersByDate> data = dashboardService.getOrdersByDate(tenantId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    @GetMapping("/top-customers")
    @Operation(summary = "Get top customers by spend")
    public ResponseEntity<ApiResponse<List<DashboardDto.TopCustomer>>> getTopCustomers(
            @RequestParam(defaultValue = "5") int limit
    ) {
        Long tenantId = TenantContext.getTenantId();
        List<DashboardDto.TopCustomer> customers = dashboardService.getTopCustomers(tenantId, limit);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }
    
    @GetMapping("/revenue-trends")
    @Operation(summary = "Get monthly revenue trends")
    public ResponseEntity<ApiResponse<List<DashboardDto.RevenueTrend>>> getRevenueTrends(
            @RequestParam(defaultValue = "12") int months
    ) {
        Long tenantId = TenantContext.getTenantId();
        List<DashboardDto.RevenueTrend> trends = dashboardService.getRevenueTrends(tenantId, months);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }
    
    @GetMapping("/top-products")
    @Operation(summary = "Get top selling products")
    public ResponseEntity<ApiResponse<List<DashboardDto.ProductPerformance>>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit
    ) {
        Long tenantId = TenantContext.getTenantId();
        List<DashboardDto.ProductPerformance> products = dashboardService.getTopProducts(tenantId, limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/order-status")
    @Operation(summary = "Get order status breakdown")
    public ResponseEntity<ApiResponse<List<DashboardDto.OrderStatusBreakdown>>> getOrderStatusBreakdown() {
        Long tenantId = TenantContext.getTenantId();
        List<DashboardDto.OrderStatusBreakdown> breakdown = dashboardService.getOrderStatusBreakdown(tenantId);
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }
    
    @GetMapping("/events")
    @Operation(summary = "Get event statistics")
    public ResponseEntity<ApiResponse<List<DashboardDto.EventStats>>> getEventStats() {
        Long tenantId = TenantContext.getTenantId();
        List<DashboardDto.EventStats> stats = dashboardService.getEventStats(tenantId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
