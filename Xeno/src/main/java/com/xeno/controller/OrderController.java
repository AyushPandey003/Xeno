package com.xeno.controller;

import com.xeno.dto.ApiResponse;
import com.xeno.dto.EntityDto;
import com.xeno.entity.Order;
import com.xeno.entity.OrderItem;
import com.xeno.repository.OrderRepository;
import com.xeno.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for order data.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    
    private final OrderRepository orderRepository;
    
    @GetMapping
    @Operation(summary = "Get all orders with pagination and date filtering")
    public ResponseEntity<ApiResponse<List<EntityDto.OrderDto>>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "processedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long tenantId = TenantContext.getTenantId();
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<Order> orders;
        if (startDate != null && endDate != null) {
            orders = orderRepository.findByDateRange(
                    tenantId, 
                    startDate.atStartOfDay(), 
                    endDate.atTime(LocalTime.MAX), 
                    pageRequest
            );
        } else {
            orders = orderRepository.findByTenantId(tenantId, pageRequest);
        }
        
        Page<EntityDto.OrderDto> dtoPage = orders.map(this::toDto);
        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<EntityDto.OrderDto>> getOrder(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        
        return orderRepository.findByTenantIdAndId(tenantId, id)
                .map(order -> ResponseEntity.ok(ApiResponse.success(toDto(order))))
                .orElse(ResponseEntity.notFound().build());
    }
    
    private EntityDto.OrderDto toDto(Order order) {
        List<EntityDto.OrderItemDto> items = order.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        
        EntityDto.CustomerSummary customer = null;
        if (order.getCustomerId() != null || order.getCustomerEmail() != null) {
            customer = EntityDto.CustomerSummary.builder()
                    .id(order.getCustomerId())
                    .email(order.getCustomerEmail())
                    .build();
        }
        
        return EntityDto.OrderDto.builder()
                .id(order.getId())
                .shopifyOrderId(order.getShopifyOrderId())
                .orderNumber(order.getOrderNumber())
                .customer(customer)
                .totalPrice(order.getTotalPrice())
                .subtotalPrice(order.getSubtotalPrice())
                .totalTax(order.getTotalTax())
                .totalDiscount(order.getTotalDiscount())
                .currency(order.getCurrency())
                .financialStatus(order.getFinancialStatus() != null ? order.getFinancialStatus().name() : null)
                .fulfillmentStatus(order.getFulfillmentStatus() != null ? order.getFulfillmentStatus().name() : null)
                .itemCount(order.getItemCount())
                .items(items)
                .processedAt(order.getProcessedAt())
                .createdAt(order.getCreatedAt())
                .build();
    }
    
    private EntityDto.OrderItemDto toItemDto(OrderItem item) {
        return EntityDto.OrderItemDto.builder()
                .id(item.getId())
                .productTitle(item.getProductTitle())
                .variantTitle(item.getVariantTitle())
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
