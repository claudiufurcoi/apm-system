package com.apm.poc.config;

import com.paypal.base.rest.APIContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Mock PayPal configuration for testing without real PayPal credentials
 * Activated with spring profile "mock"
 */
@Configuration
@Profile("mock")
@Slf4j
public class MockPayPalConfig {

    @Bean
    public APIContext apiContext() {
        log.warn("🎭 MOCK MODE ENABLED: Using mock PayPal configuration");
        log.warn("🎭 MOCK MODE: No real PayPal API calls will be made");
        log.warn("🎭 MOCK MODE: All payments will be simulated locally");

        // Return a dummy APIContext that won't be used
        // This satisfies the dependency injection requirement
        return new APIContext("mock-client-id", "mock-client-secret", "sandbox");
    }
}

