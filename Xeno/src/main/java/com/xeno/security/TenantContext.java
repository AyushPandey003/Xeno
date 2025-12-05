package com.xeno.security;

/**
 * Thread-local storage for tenant context.
 * Allows access to current tenant ID throughout the request lifecycle.
 */
public class TenantContext {
    
    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    
    public static Long getTenantId() {
        return currentTenantId.get();
    }
    
    public static void setTenantId(Long tenantId) {
        currentTenantId.set(tenantId);
    }
    
    public static Long getUserId() {
        return currentUserId.get();
    }
    
    public static void setUserId(Long userId) {
        currentUserId.set(userId);
    }
    
    public static void clear() {
        currentTenantId.remove();
        currentUserId.remove();
    }
}
