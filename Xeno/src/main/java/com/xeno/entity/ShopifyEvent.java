package com.xeno.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ShopifyEvent entity for tracking custom events.
 * Supports events like cart_abandoned, checkout_started, etc.
 */
@Entity
@Table(name = "shopify_events", indexes = {
    @Index(name = "idx_event_tenant", columnList = "tenantId"),
    @Index(name = "idx_event_type", columnList = "tenantId, eventType"),
    @Index(name = "idx_event_customer", columnList = "tenantId, customerId"),
    @Index(name = "idx_event_occurred_at", columnList = "tenantId, occurredAt DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopifyEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;
    
    private Long customerId;
    
    private String customerEmail;
    
    private Long orderId;
    
    private Long productId;
    
    @Column(columnDefinition = "TEXT")
    private String eventData;
    
    private String sessionId;
    
    private String source;
    
    private LocalDateTime occurredAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum EventType {
        CART_ABANDONED,
        CHECKOUT_STARTED,
        CHECKOUT_COMPLETED,
        PRODUCT_VIEWED,
        PRODUCT_ADDED_TO_CART,
        PRODUCT_REMOVED_FROM_CART,
        SEARCH_PERFORMED,
        COLLECTION_VIEWED,
        PAGE_VIEWED,
        CUSTOMER_REGISTERED,
        CUSTOMER_LOGGED_IN,
        ORDER_PLACED,
        ORDER_CANCELLED,
        REFUND_CREATED
    }
}
