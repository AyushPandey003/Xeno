package com.xeno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Xeno Shopify Integration - Main Application
 * 
 * Multi-tenant Shopify Data Ingestion and Insights Service
 */
@SpringBootApplication
@EnableScheduling
public class XenoApplication {

    public static void main(String[] args) {
        SpringApplication.run(XenoApplication.class, args);
    }
}
