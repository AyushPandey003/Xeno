package com.xeno.scheduler;

import com.xeno.entity.Tenant;
import com.xeno.repository.TenantRepository;
import com.xeno.service.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job for syncing data from Shopify.
 * Runs every 15 minutes for all active and connected tenants.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "scheduler.sync.enabled", havingValue = "true", matchIfMissing = true)
public class DataSyncScheduler {
    
    private final TenantRepository tenantRepository;
    private final DataIngestionService dataIngestionService;
    
    /**
     * Sync all active tenants every 15 minutes
     */
    @Scheduled(cron = "${scheduler.sync.cron:0 */15 * * * *}")
    public void syncAllTenants() {
        log.info("Starting scheduled data sync for all tenants");
        
        List<Tenant> tenants = tenantRepository.findTenantsForSync();
        log.info("Found {} tenants to sync", tenants.size());
        
        for (Tenant tenant : tenants) {
            try {
                log.info("Syncing tenant: {} ({})", tenant.getName(), tenant.getId());
                dataIngestionService.syncAllData(tenant.getId());
            } catch (Exception e) {
                log.error("Failed to sync tenant {}: {}", tenant.getId(), e.getMessage());
            }
        }
        
        log.info("Scheduled sync completed");
    }
}
