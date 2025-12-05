package com.xeno.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity synced from Shopify.
 * Stores product catalog with tenant isolation.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_tenant", columnList = "tenantId"),
    @Index(name = "idx_product_shopify_id", columnList = "tenantId, shopifyProductId", unique = true),
    @Index(name = "idx_product_vendor", columnList = "tenantId, vendor"),
    @Index(name = "idx_product_type", columnList = "tenantId, productType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tenantId;
    
    @Column(nullable = false)
    private Long shopifyProductId;
    
    private Long shopifyVariantId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    private String vendor;
    
    private String productType;
    
    private String handle;
    
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;
    
    private BigDecimal compareAtPrice;
    
    private String sku;
    
    @Builder.Default
    private Integer inventoryQuantity = 0;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
    
    @Column(length = 1000)
    private String imageUrl;
    
    private String tags;
    
    @Builder.Default
    private BigDecimal weight = BigDecimal.ZERO;
    
    private String weightUnit;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime shopifyCreatedAt;
    
    private LocalDateTime shopifyUpdatedAt;
    
    public enum ProductStatus {
        ACTIVE,
        DRAFT,
        ARCHIVED
    }
}
