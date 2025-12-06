package com.xeno.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Automatically loads environment variables from .env file
 * This eliminates the need to manually set env vars in terminal
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        try {
            // Load .env file from project root or current directory
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // Don't fail if .env doesn't exist (useful for production)
                    .load();
            
            // Convert dotenv entries to a map
            Map<String, Object> dotenvMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Only add if not already set in system environment
                if (System.getenv(key) == null) {
                    dotenvMap.put(key, value);
                    
                    // Also set in system properties for backward compatibility
                    System.setProperty(key, value);
                }
            });
            
            // Add to Spring Environment with high priority
            if (!dotenvMap.isEmpty()) {
                environment.getPropertySources()
                        .addFirst(new MapPropertySource("dotenvProperties", dotenvMap));
                
                System.out.println("✅ Loaded " + dotenvMap.size() + " variables from .env file");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Could not load .env file: " + e.getMessage());
            System.err.println("    Application will use system environment variables or defaults");
        }
    }
}
