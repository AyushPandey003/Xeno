package com.xeno.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EntityDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDto {
        private Long id;
        private Long shopifyCustomerId;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String country;
        private BigDecimal totalSpent;
        private Integer ordersCount;
        private Boolean acceptsMarketing;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDto {
        private Long id;
        private Long shopifyProductId;
        private String title;
        private String description;
        private String vendor;
        private String productType;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private String sku;
        private Integer inventoryQuantity;
        private String status;
        private String imageUrl;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDto {
        private Long id;
        private Long shopifyOrderId;
        private String orderNumber;
        private CustomerSummary customer;
        private BigDecimal totalPrice;
        private BigDecimal subtotalPrice;
        private BigDecimal totalTax;
        private BigDecimal totalDiscount;
        private String currency;
        private String financialStatus;
        private String fulfillmentStatus;
        private Integer itemCount;
        private List<OrderItemDto> items;
        private LocalDateTime processedAt;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSummary {
        private Long id;
        private String name;
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private Long id;
        private String productTitle;
        private String variantTitle;
        private String sku;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal lineTotal;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopifyEventDto {
        private Long id;
        private String eventType;
        private Long customerId;
        private String customerEmail;
        private Long orderId;
        private Long productId;
        private String eventData;
        private LocalDateTime occurredAt;
    }
}
