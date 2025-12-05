package com.xeno.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * OrderItem entity representing line items in an order.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_product", columnList = "productId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    private Long productId;
    
    private Long shopifyProductId;
    
    private Long shopifyVariantId;
    
    private Long shopifyLineItemId;
    
    private String productTitle;
    
    private String variantTitle;
    
    private String sku;
    
    @Builder.Default
    private Integer quantity = 1;
    
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    
    public BigDecimal getLineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity)).subtract(totalDiscount);
    }
}
