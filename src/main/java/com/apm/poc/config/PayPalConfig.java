package com.apm.poc.config;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!mock & !mock-applepay")
public class PayPalConfig {

    @Bean
    public Map<String, String> paypalSdkConfig(PayPalProperties properties) {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("mode", properties.getMode());
        return configMap;
    }

    @Bean
    public OAuthTokenCredential oAuthTokenCredential(PayPalProperties properties, Map<String, String> paypalSdkConfig) {
        return new OAuthTokenCredential(
                properties.getClientId(),
                properties.getClientSecret(),
                paypalSdkConfig
        );
    }

    @Bean
    public APIContext apiContext(OAuthTokenCredential oAuthTokenCredential, PayPalProperties properties) throws PayPalRESTException {
        APIContext context = new APIContext(oAuthTokenCredential.getAccessToken());
        context.setConfigurationMap(paypalSdkConfig(properties));
        return context;
    }
}

@Component
@ConfigurationProperties(prefix = "paypal")
@Data
class PayPalProperties {
    private String mode = "sandbox"; // sandbox or live
    private String clientId;
    private String clientSecret;
    private String returnUrl;
    private String cancelUrl;
}

