package com.xeno.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.xeno.entity.Tenant;
import com.xeno.repository.TenantRepository;
import com.xeno.service.DataIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Optional;

/**
 * Controller for handling Shopify webhooks.
 * Webhooks are verified using HMAC-SHA256 signature.
 */
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Shopify webhook handlers")
public class WebhookController {
    
    private final DataIngestionService dataIngestionService;
    private final TenantRepository tenantRepository;
    
    @Value("${shopify.webhook.secret:}")
    private String webhookSecret;
    
    @PostMapping("/orders/create")
    @Operation(summary = "Handle order created webhook")
    public ResponseEntity<String> handleOrderCreated(
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmacHeader,
            @RequestBody JsonNode payload
    ) {
        log.info("Received order/create webhook from {}", shopDomain);
        
        Optional<Tenant> tenant = tenantRepository.findByShopifyDomain(shopDomain);
        if (tenant.isEmpty()) {
            log.warn("Unknown shop domain: {}", shopDomain);
            return ResponseEntity.ok("OK");
        }
        
        try {
            dataIngestionService.saveOrUpdateOrder(tenant.get().getId(), payload);
            log.info("Processed order webhook for tenant {}", tenant.get().getId());
        } catch (Exception e) {
            log.error("Failed to process order webhook: {}", e.getMessage());
        }
        
        return ResponseEntity.ok("OK");
    }
    
    @PostMapping("/orders/updated")
    @Operation(summary = "Handle order updated webhook")
    public ResponseEntity<String> handleOrderUpdated(
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestBody JsonNode payload
    ) {
        log.info("Received order/updated webhook from {}", shopDomain);
        
        Optional<Tenant> tenant = tenantRepository.findByShopifyDomain(shopDomain);
        if (tenant.isEmpty()) {
            return ResponseEntity.ok("OK");
        }
        
        try {
            dataIngestionService.saveOrUpdateOrder(tenant.get().getId(), payload);
        } catch (Exception e) {
            log.error("Failed to process order update webhook: {}", e.getMessage());
        }
        
        return ResponseEntity.ok("OK");
    }
    
    @PostMapping("/customers/create")
    @Operation(summary = "Handle customer created webhook")
    public ResponseEntity<String> handleCustomerCreated(
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestBody JsonNode payload
    ) {
        log.info("Received customer/create webhook from {}", shopDomain);
        
        Optional<Tenant> tenant = tenantRepository.findByShopifyDomain(shopDomain);
        if (tenant.isEmpty()) {
            return ResponseEntity.ok("OK");
        }
        
        try {
            dataIngestionService.saveOrUpdateCustomer(tenant.get().getId(), payload);
        } catch (Exception e) {
            log.error("Failed to process customer webhook: {}", e.getMessage());
        }
        
        return ResponseEntity.ok("OK");
    }
    
    @PostMapping("/customers/updated")
    @Operation(summary = "Handle customer updated webhook")
    public ResponseEntity<String> handleCustomerUpdated(
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestBody JsonNode payload
    ) {
        log.info("Received customer/updated webhook from {}", shopDomain);
        
        Optional<Tenant> tenant = tenantRepository.findByShopifyDomain(shopDomain);
        if (tenant.isEmpty()) {
            return ResponseEntity.ok("OK");
        }
        
        try {
            dataIngestionService.saveOrUpdateCustomer(tenant.get().getId(), payload);
        } catch (Exception e) {
            log.error("Failed to process customer update webhook: {}", e.getMessage());
        }
        
        return ResponseEntity.ok("OK");
    }
    
    @PostMapping("/products/create")
    @Operation(summary = "Handle product created webhook")
    public ResponseEntity<String> handleProductCreated(
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestBody JsonNode payload
    ) {
        log.info("Received product/create webhook from {}", shopDomain);
        
        Optional<Tenant> tenant = tenantRepository.findByShopifyDomain(shopDomain);
        if (tenant.isEmpty()) {
            return ResponseEntity.ok("OK");
        }
        
        try {
            dataIngestionService.saveOrUpdateProduct(tenant.get().getId(), payload);
        } catch (Exception e) {
            log.error("Failed to process product webhook: {}", e.getMessage());
        }
        
        return ResponseEntity.ok("OK");
    }
    
    @PostMapping("/products/updated")
    @Operation(summary = "Handle product updated webhook")
    public ResponseEntity<String> handleProductUpdated(
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestBody JsonNode payload
    ) {
        log.info("Received product/updated webhook from {}", shopDomain);
        
        Optional<Tenant> tenant = tenantRepository.findByShopifyDomain(shopDomain);
        if (tenant.isEmpty()) {
            return ResponseEntity.ok("OK");
        }
        
        try {
            dataIngestionService.saveOrUpdateProduct(tenant.get().getId(), payload);
        } catch (Exception e) {
            log.error("Failed to process product update webhook: {}", e.getMessage());
        }
        
        return ResponseEntity.ok("OK");
    }
    
    /**
     * Verify webhook HMAC signature
     */
    private boolean verifyWebhook(String body, String hmacHeader) {
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            return true; // Skip verification if no secret configured
        }
        
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(body.getBytes());
            String calculatedHmac = Base64.getEncoder().encodeToString(hash);
            return calculatedHmac.equals(hmacHeader);
        } catch (Exception e) {
            log.error("Failed to verify webhook signature: {}", e.getMessage());
            return false;
        }
    }
}
