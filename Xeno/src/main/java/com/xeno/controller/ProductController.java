package com.xeno.controller;

import com.xeno.dto.ApiResponse;
import com.xeno.dto.EntityDto;
import com.xeno.entity.Product;
import com.xeno.repository.ProductRepository;
import com.xeno.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for product data.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private final ProductRepository productRepository;
    
    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<ApiResponse<List<EntityDto.ProductDto>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        Long tenantId = TenantContext.getTenantId();
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.searchByTenantId(tenantId, search, pageRequest);
        } else {
            products = productRepository.findByTenantId(tenantId, pageRequest);
        }
        
        Page<EntityDto.ProductDto> dtoPage = products.map(this::toDto);
        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<EntityDto.ProductDto>> getProduct(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        
        return productRepository.findByTenantIdAndId(tenantId, id)
                .map(product -> ResponseEntity.ok(ApiResponse.success(toDto(product))))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/vendors")
    @Operation(summary = "Get list of distinct vendors")
    public ResponseEntity<ApiResponse<List<String>>> getVendors() {
        Long tenantId = TenantContext.getTenantId();
        List<String> vendors = productRepository.findDistinctVendors(tenantId);
        return ResponseEntity.ok(ApiResponse.success(vendors));
    }
    
    @GetMapping("/types")
    @Operation(summary = "Get list of distinct product types")
    public ResponseEntity<ApiResponse<List<String>>> getProductTypes() {
        Long tenantId = TenantContext.getTenantId();
        List<String> types = productRepository.findDistinctProductTypes(tenantId);
        return ResponseEntity.ok(ApiResponse.success(types));
    }
    
    @GetMapping("/low-inventory")
    @Operation(summary = "Get products with low inventory")
    public ResponseEntity<ApiResponse<List<EntityDto.ProductDto>>> getLowInventory(
            @RequestParam(defaultValue = "10") int threshold
    ) {
        Long tenantId = TenantContext.getTenantId();
        List<Product> products = productRepository.findLowInventory(tenantId, threshold);
        List<EntityDto.ProductDto> dtos = products.stream().map(this::toDto).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
    
    private EntityDto.ProductDto toDto(Product product) {
        return EntityDto.ProductDto.builder()
                .id(product.getId())
                .shopifyProductId(product.getShopifyProductId())
                .title(product.getTitle())
                .description(product.getDescription())
                .vendor(product.getVendor())
                .productType(product.getProductType())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .sku(product.getSku())
                .inventoryQuantity(product.getInventoryQuantity())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
