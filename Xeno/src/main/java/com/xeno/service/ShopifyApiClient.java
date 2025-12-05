package com.xeno.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Shopify API Client for making REST API calls to Shopify Admin API.
 */
@Service
@Slf4j
public class ShopifyApiClient {
    
    @Value("${shopify.api.version}")
    private String apiVersion;
    
    private final ObjectMapper objectMapper;
    
    public ShopifyApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Create a WebClient configured for a specific Shopify store
     */
    private WebClient createClient(String shopDomain, String accessToken) {
        String baseUrl = String.format("https://%s/admin/api/%s", shopDomain, apiVersion);
        
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Shopify-Access-Token", accessToken)
                .build();
    }
    
    /**
     * Verify Shopify connection by fetching shop info
     */
    public Mono<JsonNode> verifyConnection(String shopDomain, String accessToken) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri("/shop.json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(e -> log.error("Failed to verify Shopify connection: {}", e.getMessage()));
    }
    
    /**
     * Fetch all customers from Shopify (paginated)
     */
    public Mono<JsonNode> getCustomers(String shopDomain, String accessToken, int limit, String pageInfo) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/customers.json").queryParam("limit", limit);
                    if (pageInfo != null) {
                        uriBuilder.queryParam("page_info", pageInfo);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(e -> log.error("Failed to fetch customers: {}", e.getMessage()));
    }
    
    /**
     * Fetch all products from Shopify (paginated)
     */
    public Mono<JsonNode> getProducts(String shopDomain, String accessToken, int limit, String pageInfo) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/products.json").queryParam("limit", limit);
                    if (pageInfo != null) {
                        uriBuilder.queryParam("page_info", pageInfo);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(e -> log.error("Failed to fetch products: {}", e.getMessage()));
    }
    
    /**
     * Fetch all orders from Shopify (paginated)
     */
    public Mono<JsonNode> getOrders(String shopDomain, String accessToken, int limit, String pageInfo, String status) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/orders.json")
                            .queryParam("limit", limit)
                            .queryParam("status", status != null ? status : "any");
                    if (pageInfo != null) {
                        uriBuilder.queryParam("page_info", pageInfo);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(e -> log.error("Failed to fetch orders: {}", e.getMessage()));
    }
    
    /**
     * Fetch a single customer by ID
     */
    public Mono<JsonNode> getCustomer(String shopDomain, String accessToken, Long customerId) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri("/customers/{id}.json", customerId)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
    
    /**
     * Fetch a single order by ID
     */
    public Mono<JsonNode> getOrder(String shopDomain, String accessToken, Long orderId) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri("/orders/{id}.json", orderId)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
    
    /**
     * Fetch a single product by ID
     */
    public Mono<JsonNode> getProduct(String shopDomain, String accessToken, Long productId) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri("/products/{id}.json", productId)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
    
    /**
     * Get orders count
     */
    public Mono<JsonNode> getOrdersCount(String shopDomain, String accessToken) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri("/orders/count.json?status=any")
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
    
    /**
     * Get customers count
     */
    public Mono<JsonNode> getCustomersCount(String shopDomain, String accessToken) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri("/customers/count.json")
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
    
    /**
     * Get products count
     */
    public Mono<JsonNode> getProductsCount(String shopDomain, String accessToken) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri("/products/count.json")
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
    
    /**
     * Register a webhook with Shopify
     */
    public Mono<JsonNode> registerWebhook(String shopDomain, String accessToken, String topic, String address) {
        Map<String, Object> webhook = Map.of(
                "webhook", Map.of(
                        "topic", topic,
                        "address", address,
                        "format", "json"
                )
        );
        
        return createClient(shopDomain, accessToken)
                .post()
                .uri("/webhooks.json")
                .bodyValue(webhook)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnSuccess(r -> log.info("Registered webhook for topic: {}", topic))
                .doOnError(e -> log.error("Failed to register webhook: {}", e.getMessage()));
    }
    
    /**
     * List all registered webhooks
     */
    public Mono<JsonNode> listWebhooks(String shopDomain, String accessToken) {
        return createClient(shopDomain, accessToken)
                .get()
                .uri("/webhooks.json")
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
}
