package com.xeno.service;

import com.xeno.dto.AuthDto;
import com.xeno.entity.Tenant;
import com.xeno.entity.User;
import com.xeno.repository.TenantRepository;
import com.xeno.repository.UserRepository;
import com.xeno.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication service handling user registration and login.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    
    /**
     * Register a new user and create their tenant (company)
     */
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create tenant (company)
        Tenant tenant = Tenant.builder()
                .name(request.getCompanyName())
                .active(true)
                .shopifyConnected(false)
                .build();
        tenant = tenantRepository.save(tenant);
        
        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .tenant(tenant)
                .role(User.Role.ADMIN)
                .active(true)
                .build();
        user = userRepository.save(user);
        
        log.info("New user registered: {} for tenant: {}", user.getEmail(), tenant.getName());
        
        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, user.getId(), tenant.getId());
        
        return buildAuthResponse(token, user);
    }
    
    /**
     * Authenticate user and return JWT token
     */
    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // Get user with tenant
        User user = userRepository.findByEmailWithTenant(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, user.getId(), user.getTenant().getId());
        
        log.info("User logged in: {}", user.getEmail());
        
        return buildAuthResponse(token, user);
    }
    
    /**
     * Get current user information
     */
    @Transactional(readOnly = true)
    public AuthDto.UserInfo getCurrentUser(String email) {
        User user = userRepository.findByEmailWithTenant(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return buildUserInfo(user);
    }
    
    private AuthDto.AuthResponse buildAuthResponse(String token, User user) {
        return AuthDto.AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(buildUserInfo(user))
                .build();
    }
    
    private AuthDto.UserInfo buildUserInfo(User user) {
        Tenant tenant = user.getTenant();
        
        return AuthDto.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .tenant(AuthDto.TenantInfo.builder()
                        .id(tenant.getId())
                        .name(tenant.getName())
                        .shopifyDomain(tenant.getShopifyDomain())
                        .shopifyConnected(tenant.getShopifyConnected())
                        .syncStatus(tenant.getSyncStatus().name())
                        .build())
                .build();
    }
}
