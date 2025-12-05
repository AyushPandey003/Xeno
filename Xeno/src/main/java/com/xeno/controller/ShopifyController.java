package com.xeno.controller;

import com.xeno.dto.ApiResponse;
import com.xeno.dto.ShopifyDto;
import com.xeno.security.TenantContext;
import com.xeno.service.DataIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Shopify store connection and data sync.
 */
@RestController
@RequestMapping("/shopify")
@RequiredArgsConstructor
@Tag(name = "Shopify", description = "Shopify store connection and data sync")
@SecurityRequirement(name = "bearerAuth")
public class ShopifyController {
    
    private final DataIngestionService dataIngestionService;
    
    @PutMapping("/connect")
    @Operation(summary = "Connect a Shopify store")
    public ResponseEntity<ApiResponse<ShopifyDto.ConnectionStatus>> connectShopify(
            @Valid @RequestBody ShopifyDto.ConnectRequest request
    ) {
        try {
            Long tenantId = TenantContext.getTenantId();
            ShopifyDto.ConnectionStatus status = dataIngestionService.connectShopify(tenantId, request);
            return ResponseEntity.ok(ApiResponse.success(status, "Shopify store connected successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get Shopify connection status")
    public ResponseEntity<ApiResponse<ShopifyDto.ConnectionStatus>> getConnectionStatus() {
        Long tenantId = TenantContext.getTenantId();
        ShopifyDto.ConnectionStatus status = dataIngestionService.getConnectionStatus(tenantId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    
    @PostMapping("/sync")
    @Operation(summary = "Trigger manual data sync from Shopify")
    public ResponseEntity<ApiResponse<ShopifyDto.SyncResult>> syncData() {
        try {
            Long tenantId = TenantContext.getTenantId();
            ShopifyDto.SyncResult result = dataIngestionService.syncAllData(tenantId);
            return ResponseEntity.ok(ApiResponse.success(result, 
                    result.getSuccess() ? "Sync completed" : "Sync failed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
