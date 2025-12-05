package com.xeno.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tenant entity representing a Shopify store.
 * Each tenant has isolated data and their own Shopify connection.
 */
@Entity
@Table(name = "tenants", indexes = {
    @Index(name = "idx_tenant_shopify_domain", columnList = "shopifyDomain", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String shopifyDomain;
    
    @Column(length = 500)
    private String shopifyAccessToken;
    
    @Column(length = 100)
    private String shopifyApiVersion;
    
    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Boolean shopifyConnected = false;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastSyncAt;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.NEVER;
    
    private String syncMessage;
    
    public enum SyncStatus {
        NEVER,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
