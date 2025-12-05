package com.xeno.controller;

import com.xeno.dto.ApiResponse;
import com.xeno.dto.EntityDto;
import com.xeno.entity.Customer;
import com.xeno.repository.CustomerRepository;
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
 * Controller for customer data.
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {
    
    private final CustomerRepository customerRepository;
    
    @GetMapping
    @Operation(summary = "Get all customers with pagination")
    public ResponseEntity<ApiResponse<List<EntityDto.CustomerDto>>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        Long tenantId = TenantContext.getTenantId();
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<Customer> customers;
        if (search != null && !search.isBlank()) {
            customers = customerRepository.searchByTenantId(tenantId, search, pageRequest);
        } else {
            customers = customerRepository.findByTenantId(tenantId, pageRequest);
        }
        
        Page<EntityDto.CustomerDto> dtoPage = customers.map(this::toDto);
        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<EntityDto.CustomerDto>> getCustomer(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        
        return customerRepository.findByTenantIdAndId(tenantId, id)
                .map(customer -> ResponseEntity.ok(ApiResponse.success(toDto(customer))))
                .orElse(ResponseEntity.notFound().build());
    }
    
    private EntityDto.CustomerDto toDto(Customer customer) {
        return EntityDto.CustomerDto.builder()
                .id(customer.getId())
                .shopifyCustomerId(customer.getShopifyCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .totalSpent(customer.getTotalSpent())
                .ordersCount(customer.getOrdersCount())
                .acceptsMarketing(customer.getAcceptsMarketing())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
