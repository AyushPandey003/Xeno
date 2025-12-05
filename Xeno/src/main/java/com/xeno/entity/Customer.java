package com.xeno.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer entity synced from Shopify.
 * Stores customer information with tenant isolation.
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_tenant", columnList = "tenantId"),
    @Index(name = "idx_customer_shopify_id", columnList = "tenantId, shopifyCustomerId", unique = true),
    @Index(name = "idx_customer_email", columnList = "tenantId, email"),
    @Index(name = "idx_customer_total_spent", columnList = "tenantId, totalSpent DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tenantId;
    
    @Column(nullable = false)
    private Long shopifyCustomerId;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String phone;
    
    @Column(length = 500)
    private String address;
    
    private String city;
    
    private String state;
    
    private String country;
    
    private String zipCode;
    
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @Builder.Default
    private Integer ordersCount = 0;
    
    @Builder.Default
    private Boolean acceptsMarketing = false;
    
    private String tags;
    
    private String note;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime shopifyCreatedAt;
    
    private LocalDateTime shopifyUpdatedAt;
    
    /**
     * Get customer's full name
     */
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null) name.append(firstName);
        if (lastName != null) {
            if (name.length() > 0) name.append(" ");
            name.append(lastName);
        }
        return name.length() > 0 ? name.toString() : email;
    }
}
