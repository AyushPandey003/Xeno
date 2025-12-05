package com.xeno.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity synced from Shopify.
 * Represents customer orders with line items.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_tenant", columnList = "tenantId"),
    @Index(name = "idx_order_shopify_id", columnList = "tenantId, shopifyOrderId", unique = true),
    @Index(name = "idx_order_customer", columnList = "tenantId, customerId"),
    @Index(name = "idx_order_processed_at", columnList = "tenantId, processedAt DESC"),
    @Index(name = "idx_order_financial_status", columnList = "tenantId, financialStatus")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tenantId;
    
    @Column(nullable = false)
    private Long shopifyOrderId;
    
    @Column(nullable = false)
    private String orderNumber;
    
    private Long customerId;
    
    private String customerEmail;
    
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal subtotalPrice = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal totalTax = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal totalShipping = BigDecimal.ZERO;
    
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FinancialStatus financialStatus = FinancialStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    private FulfillmentStatus fulfillmentStatus;
    
    @Builder.Default
    private Integer itemCount = 0;
    
    private String shippingAddress;
    
    private String billingAddress;
    
    private String note;
    
    private String tags;
    
    private String source;
    
    @Builder.Default
    private Boolean confirmed = false;
    
    @Builder.Default
    private Boolean cancelled = false;
    
    private LocalDateTime cancelledAt;
    
    private String cancelReason;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    
    private LocalDateTime processedAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime shopifyCreatedAt;
    
    private LocalDateTime shopifyUpdatedAt;
    
    public enum FinancialStatus {
        PENDING,
        AUTHORIZED,
        PARTIALLY_PAID,
        PAID,
        PARTIALLY_REFUNDED,
        REFUNDED,
        VOIDED
    }
    
    public enum FulfillmentStatus {
        UNFULFILLED,
        PARTIAL,
        FULFILLED,
        RESTOCKED
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
