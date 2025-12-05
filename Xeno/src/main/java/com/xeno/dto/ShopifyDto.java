package com.xeno.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ShopifyDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectRequest {
        @NotBlank(message = "Shop domain is required")
        private String shopDomain;
        
        @NotBlank(message = "Access token is required")
        private String accessToken;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionStatus {
        private Boolean connected;
        private String shopDomain;
        private String syncStatus;
        private LocalDateTime lastSyncAt;
        private String syncMessage;
        private SyncStats stats;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncStats {
        private Long customersCount;
        private Long productsCount;
        private Long ordersCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResult {
        private Boolean success;
        private String message;
        private Integer customersImported;
        private Integer productsImported;
        private Integer ordersImported;
        private LocalDateTime completedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookPayload {
        private Long id;
        private String topic;
        private Object data;
    }
}
