package com.xeno.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.xeno.dto.ShopifyDto;
import com.xeno.entity.*;
import com.xeno.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for ingesting data from Shopify into the local database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService {
    
    private final ShopifyApiClient shopifyApiClient;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    
    private static final int PAGE_SIZE = 250;
    private static final DateTimeFormatter SHOPIFY_DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
    
    /**
     * Connect a Shopify store to a tenant
     */
    @Transactional
    public ShopifyDto.ConnectionStatus connectShopify(Long tenantId, ShopifyDto.ConnectRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        // Verify connection with Shopify
        try {
            JsonNode shopInfo = shopifyApiClient.verifyConnection(
                    request.getShopDomain(), 
                    request.getAccessToken()
            ).block();
            
            if (shopInfo == null || !shopInfo.has("shop")) {
                throw new RuntimeException("Invalid Shopify credentials");
            }
            
            // Update tenant with Shopify connection
            tenant.setShopifyDomain(request.getShopDomain());
            tenant.setShopifyAccessToken(request.getAccessToken());
            tenant.setShopifyConnected(true);
            tenant.setShopifyApiVersion("2024-01");
            tenantRepository.save(tenant);
            
            log.info("Connected Shopify store {} for tenant {}", request.getShopDomain(), tenantId);
            
            return ShopifyDto.ConnectionStatus.builder()
                    .connected(true)
                    .shopDomain(request.getShopDomain())
                    .syncStatus(tenant.getSyncStatus().name())
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to connect Shopify: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to Shopify: " + e.getMessage());
        }
    }
    
    /**
     * Get current Shopify connection status
     */
    @Transactional(readOnly = true)
    public ShopifyDto.ConnectionStatus getConnectionStatus(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        ShopifyDto.SyncStats stats = ShopifyDto.SyncStats.builder()
                .customersCount(customerRepository.countByTenantId(tenantId))
                .productsCount(productRepository.countByTenantId(tenantId))
                .ordersCount(orderRepository.countByTenantId(tenantId))
                .build();
        
        return ShopifyDto.ConnectionStatus.builder()
                .connected(tenant.getShopifyConnected())
                .shopDomain(tenant.getShopifyDomain())
                .syncStatus(tenant.getSyncStatus().name())
                .lastSyncAt(tenant.getLastSyncAt())
                .syncMessage(tenant.getSyncMessage())
                .stats(stats)
                .build();
    }
    
    /**
     * Sync all data from Shopify for a tenant
     */
    @Transactional
    public ShopifyDto.SyncResult syncAllData(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        if (!tenant.getShopifyConnected()) {
            throw new RuntimeException("Shopify not connected for this tenant");
        }
        
        // Update sync status
        tenant.setSyncStatus(Tenant.SyncStatus.IN_PROGRESS);
        tenant.setSyncMessage("Sync in progress...");
        tenantRepository.save(tenant);
        
        int customersImported = 0;
        int productsImported = 0;
        int ordersImported = 0;
        
        try {
            // Sync customers
            customersImported = syncCustomers(tenant);
            
            // Sync products
            productsImported = syncProducts(tenant);
            
            // Sync orders
            ordersImported = syncOrders(tenant);
            
            // Update sync status
            tenant.setSyncStatus(Tenant.SyncStatus.COMPLETED);
            tenant.setLastSyncAt(LocalDateTime.now());
            tenant.setSyncMessage(String.format("Successfully synced %d customers, %d products, %d orders",
                    customersImported, productsImported, ordersImported));
            tenantRepository.save(tenant);
            
            log.info("Sync completed for tenant {}: {} customers, {} products, {} orders",
                    tenantId, customersImported, productsImported, ordersImported);
            
            return ShopifyDto.SyncResult.builder()
                    .success(true)
                    .message("Sync completed successfully")
                    .customersImported(customersImported)
                    .productsImported(productsImported)
                    .ordersImported(ordersImported)
                    .completedAt(LocalDateTime.now())
                    .build();
            
        } catch (Exception e) {
            log.error("Sync failed for tenant {}: {}", tenantId, e.getMessage());
            
            tenant.setSyncStatus(Tenant.SyncStatus.FAILED);
            tenant.setSyncMessage("Sync failed: " + e.getMessage());
            tenantRepository.save(tenant);
            
            return ShopifyDto.SyncResult.builder()
                    .success(false)
                    .message("Sync failed: " + e.getMessage())
                    .customersImported(customersImported)
                    .productsImported(productsImported)
                    .ordersImported(ordersImported)
                    .completedAt(LocalDateTime.now())
                    .build();
        }
    }
    
    /**
     * Sync customers from Shopify
     */
    private int syncCustomers(Tenant tenant) {
        int count = 0;
        String pageInfo = null;
        
        do {
            JsonNode response = shopifyApiClient.getCustomers(
                    tenant.getShopifyDomain(),
                    tenant.getShopifyAccessToken(),
                    PAGE_SIZE,
                    pageInfo
            ).block();
            
            if (response != null && response.has("customers")) {
                for (JsonNode customerNode : response.get("customers")) {
                    saveOrUpdateCustomer(tenant.getId(), customerNode);
                    count++;
                }
            }
            
            // TODO: Handle pagination with Link header
            pageInfo = null; // For now, just get first page
            
        } while (pageInfo != null);
        
        return count;
    }
    
    /**
     * Sync products from Shopify
     */
    private int syncProducts(Tenant tenant) {
        int count = 0;
        String pageInfo = null;
        
        do {
            JsonNode response = shopifyApiClient.getProducts(
                    tenant.getShopifyDomain(),
                    tenant.getShopifyAccessToken(),
                    PAGE_SIZE,
                    pageInfo
            ).block();
            
            if (response != null && response.has("products")) {
                for (JsonNode productNode : response.get("products")) {
                    saveOrUpdateProduct(tenant.getId(), productNode);
                    count++;
                }
            }
            
            pageInfo = null;
            
        } while (pageInfo != null);
        
        return count;
    }
    
    /**
     * Sync orders from Shopify
     */
    private int syncOrders(Tenant tenant) {
        int count = 0;
        String pageInfo = null;
        
        do {
            JsonNode response = shopifyApiClient.getOrders(
                    tenant.getShopifyDomain(),
                    tenant.getShopifyAccessToken(),
                    PAGE_SIZE,
                    pageInfo,
                    "any"
            ).block();
            
            if (response != null && response.has("orders")) {
                for (JsonNode orderNode : response.get("orders")) {
                    saveOrUpdateOrder(tenant.getId(), orderNode);
                    count++;
                }
            }
            
            pageInfo = null;
            
        } while (pageInfo != null);
        
        return count;
    }
    
    /**
     * Save or update a customer from Shopify data
     */
    @Transactional
    public void saveOrUpdateCustomer(Long tenantId, JsonNode data) {
        Long shopifyId = data.get("id").asLong();
        
        Customer customer = customerRepository.findByTenantIdAndShopifyCustomerId(tenantId, shopifyId)
                .orElse(Customer.builder()
                        .tenantId(tenantId)
                        .shopifyCustomerId(shopifyId)
                        .build());
        
        customer.setEmail(getTextValue(data, "email"));
        customer.setFirstName(getTextValue(data, "first_name"));
        customer.setLastName(getTextValue(data, "last_name"));
        customer.setPhone(getTextValue(data, "phone"));
        customer.setTotalSpent(getBigDecimalValue(data, "total_spent"));
        customer.setOrdersCount(getIntValue(data, "orders_count"));
        customer.setAcceptsMarketing(getBooleanValue(data, "accepts_marketing"));
        customer.setTags(getTextValue(data, "tags"));
        customer.setNote(getTextValue(data, "note"));
        
        // Address
        if (data.has("default_address") && !data.get("default_address").isNull()) {
            JsonNode addr = data.get("default_address");
            customer.setAddress(getTextValue(addr, "address1"));
            customer.setCity(getTextValue(addr, "city"));
            customer.setState(getTextValue(addr, "province"));
            customer.setCountry(getTextValue(addr, "country"));
            customer.setZipCode(getTextValue(addr, "zip"));
        }
        
        customer.setShopifyCreatedAt(getDateTimeValue(data, "created_at"));
        customer.setShopifyUpdatedAt(getDateTimeValue(data, "updated_at"));
        
        customerRepository.save(customer);
    }
    
    /**
     * Save or update a product from Shopify data
     */
    @Transactional
    public void saveOrUpdateProduct(Long tenantId, JsonNode data) {
        Long shopifyId = data.get("id").asLong();
        
        Product product = productRepository.findByTenantIdAndShopifyProductId(tenantId, shopifyId)
                .orElse(Product.builder()
                        .tenantId(tenantId)
                        .shopifyProductId(shopifyId)
                        .build());
        
        product.setTitle(getTextValue(data, "title"));
        product.setDescription(getTextValue(data, "body_html"));
        product.setVendor(getTextValue(data, "vendor"));
        product.setProductType(getTextValue(data, "product_type"));
        product.setHandle(getTextValue(data, "handle"));
        product.setTags(getTextValue(data, "tags"));
        product.setStatus(parseProductStatus(getTextValue(data, "status")));
        
        // Get first variant for price
        if (data.has("variants") && data.get("variants").isArray() && data.get("variants").size() > 0) {
            JsonNode variant = data.get("variants").get(0);
            product.setShopifyVariantId(variant.get("id").asLong());
            product.setPrice(getBigDecimalValue(variant, "price"));
            product.setCompareAtPrice(getBigDecimalValue(variant, "compare_at_price"));
            product.setSku(getTextValue(variant, "sku"));
            product.setInventoryQuantity(getIntValue(variant, "inventory_quantity"));
            product.setWeight(getBigDecimalValue(variant, "weight"));
            product.setWeightUnit(getTextValue(variant, "weight_unit"));
        }
        
        // Get first image
        if (data.has("images") && data.get("images").isArray() && data.get("images").size() > 0) {
            product.setImageUrl(getTextValue(data.get("images").get(0), "src"));
        }
        
        product.setShopifyCreatedAt(getDateTimeValue(data, "created_at"));
        product.setShopifyUpdatedAt(getDateTimeValue(data, "updated_at"));
        
        productRepository.save(product);
    }
    
    /**
     * Save or update an order from Shopify data
     */
    @Transactional
    public void saveOrUpdateOrder(Long tenantId, JsonNode data) {
        Long shopifyId = data.get("id").asLong();
        
        Order order = orderRepository.findByTenantIdAndShopifyOrderId(tenantId, shopifyId)
                .orElse(Order.builder()
                        .tenantId(tenantId)
                        .shopifyOrderId(shopifyId)
                        .build());
        
        order.setOrderNumber(getTextValue(data, "order_number"));
        order.setTotalPrice(getBigDecimalValue(data, "total_price"));
        order.setSubtotalPrice(getBigDecimalValue(data, "subtotal_price"));
        order.setTotalTax(getBigDecimalValue(data, "total_tax"));
        order.setTotalDiscount(getBigDecimalValue(data, "total_discounts"));
        order.setCurrency(getTextValue(data, "currency"));
        order.setFinancialStatus(parseFinancialStatus(getTextValue(data, "financial_status")));
        order.setFulfillmentStatus(parseFulfillmentStatus(getTextValue(data, "fulfillment_status")));
        order.setNote(getTextValue(data, "note"));
        order.setTags(getTextValue(data, "tags"));
        order.setSource(getTextValue(data, "source_name"));
        order.setConfirmed(getBooleanValue(data, "confirmed"));
        order.setCancelled(data.has("cancelled_at") && !data.get("cancelled_at").isNull());
        
        if (order.getCancelled()) {
            order.setCancelledAt(getDateTimeValue(data, "cancelled_at"));
            order.setCancelReason(getTextValue(data, "cancel_reason"));
        }
        
        // Customer info
        if (data.has("customer") && !data.get("customer").isNull()) {
            JsonNode customerNode = data.get("customer");
            Long shopifyCustomerId = customerNode.get("id").asLong();
            order.setCustomerEmail(getTextValue(customerNode, "email"));
            
            // Link to internal customer if exists
            customerRepository.findByTenantIdAndShopifyCustomerId(tenantId, shopifyCustomerId)
                    .ifPresent(c -> order.setCustomerId(c.getId()));
        }
        
        order.setProcessedAt(getDateTimeValue(data, "processed_at"));
        order.setShopifyCreatedAt(getDateTimeValue(data, "created_at"));
        order.setShopifyUpdatedAt(getDateTimeValue(data, "updated_at"));
        
        // Save order first
        Order savedOrder = orderRepository.save(order);
        
        // Process line items
        if (data.has("line_items") && data.get("line_items").isArray()) {
            // Clear existing items
            savedOrder.getItems().clear();
            
            int itemCount = 0;
            for (JsonNode itemNode : data.get("line_items")) {
                OrderItem item = OrderItem.builder()
                        .order(savedOrder)
                        .shopifyLineItemId(itemNode.get("id").asLong())
                        .shopifyProductId(itemNode.has("product_id") ? itemNode.get("product_id").asLong() : null)
                        .shopifyVariantId(itemNode.has("variant_id") ? itemNode.get("variant_id").asLong() : null)
                        .productTitle(getTextValue(itemNode, "title"))
                        .variantTitle(getTextValue(itemNode, "variant_title"))
                        .sku(getTextValue(itemNode, "sku"))
                        .quantity(getIntValue(itemNode, "quantity"))
                        .price(getBigDecimalValue(itemNode, "price"))
                        .totalDiscount(getBigDecimalValue(itemNode, "total_discount"))
                        .build();
                
                // Link to internal product if exists
                if (item.getShopifyProductId() != null) {
                    productRepository.findByTenantIdAndShopifyProductId(tenantId, item.getShopifyProductId())
                            .ifPresent(p -> item.setProductId(p.getId()));
                }
                
                savedOrder.addItem(item);
                itemCount += item.getQuantity();
            }
            
            savedOrder.setItemCount(itemCount);
            orderRepository.save(savedOrder);
        }
    }
    
    // Helper methods for parsing JSON
    private String getTextValue(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }
    
    private Integer getIntValue(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asInt() : 0;
    }
    
    private Boolean getBooleanValue(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() && node.get(field).asBoolean();
    }
    
    private BigDecimal getBigDecimalValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            String value = node.get(field).asText();
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    private LocalDateTime getDateTimeValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            try {
                return LocalDateTime.parse(node.get(field).asText(), SHOPIFY_DATE_FORMAT);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    private Product.ProductStatus parseProductStatus(String status) {
        if (status == null) return Product.ProductStatus.ACTIVE;
        return switch (status.toLowerCase()) {
            case "draft" -> Product.ProductStatus.DRAFT;
            case "archived" -> Product.ProductStatus.ARCHIVED;
            default -> Product.ProductStatus.ACTIVE;
        };
    }
    
    private Order.FinancialStatus parseFinancialStatus(String status) {
        if (status == null) return Order.FinancialStatus.PENDING;
        return switch (status.toLowerCase()) {
            case "authorized" -> Order.FinancialStatus.AUTHORIZED;
            case "partially_paid" -> Order.FinancialStatus.PARTIALLY_PAID;
            case "paid" -> Order.FinancialStatus.PAID;
            case "partially_refunded" -> Order.FinancialStatus.PARTIALLY_REFUNDED;
            case "refunded" -> Order.FinancialStatus.REFUNDED;
            case "voided" -> Order.FinancialStatus.VOIDED;
            default -> Order.FinancialStatus.PENDING;
        };
    }
    
    private Order.FulfillmentStatus parseFulfillmentStatus(String status) {
        if (status == null) return null;
        return switch (status.toLowerCase()) {
            case "partial" -> Order.FulfillmentStatus.PARTIAL;
            case "fulfilled" -> Order.FulfillmentStatus.FULFILLED;
            case "restocked" -> Order.FulfillmentStatus.RESTOCKED;
            default -> Order.FulfillmentStatus.UNFULFILLED;
        };
    }
}
