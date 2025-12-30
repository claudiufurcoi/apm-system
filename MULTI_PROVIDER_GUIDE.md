# Multi-Provider Payment System Guide

## Overview
The payment system now supports multiple payment providers through a common interface. You can easily switch between providers or add new ones.

## Available Payment Providers

### 1. **Mock Service** (Default for Testing)
- **Profile**: `mock`
- **Use Case**: Local development and testing without real payment credentials
- **Bean Name**: `payPalService`

### 2. **PayPal Service**
- **Profile**: `prod` (or `!mock`)
- **Use Case**: Production PayPal payments
- **Bean Name**: `payPalService`
- **Requirements**: Valid PayPal API credentials

### 3. **Apple Pay Service** (Example Implementation)
- **Profile**: `applepay`
- **Use Case**: Apple Pay payments
- **Bean Name**: `applePayService`
- **Requirements**: Apple Pay merchant certificate and payment processor

## Running with Different Providers

### Mock Mode (No credentials needed)
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=mock

# Using Java
java -jar target/apm-1.0-SNAPSHOT.jar --spring.profiles.active=mock
```

### PayPal Mode (Production)
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Using Java
java -jar target/apm-1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Apple Pay Mode
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=applepay

# Using Java
java -jar target/apm-1.0-SNAPSHOT.jar --spring.profiles.active=applepay
```

## Architecture

### Interface: `PaymentServiceInterface`
```java
public interface PaymentServiceInterface {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse executePayment(String paymentId, String payerId);
    Payment getPaymentDetails(String paymentId);
}
```

### Implementations:
1. **PayPalService** - Real PayPal integration
2. **MockPayPalService** - Mock for testing
3. **ApplePayService** - Apple Pay integration (skeleton)

### Dependency Injection
The controller uses Spring's dependency injection to automatically wire the correct implementation based on the active profile:

```java
@RestController
public class PaymentController {
    private final PaymentServiceInterface paymentService;  // Injected based on profile
}
```

## API Endpoints

All endpoints remain the same regardless of payment provider:

### Create Payment
```http
POST /api/payment/create
Content-Type: application/json

{
  "amount": 100.00,
  "currency": "USD",
  "description": "Product purchase",
  "orderID": "ORDER-123",
  "userEmail": "customer@example.com"
}
```

### Payment Success Callback
```http
GET /api/payment/success?paymentId=PAY-XXX&PayerID=PAYER-123
```

### Payment Cancel Callback
```http
GET /api/payment/cancel
```

### Get Payment Details
```http
GET /api/payment/{paymentId}
```

### Health Check
```http
GET /api/payment/health
```

## Adding a New Payment Provider

### Step 1: Create Service Class
```java
@Service("stripeService")  // Unique bean name
@Profile("stripe")          // Profile name
@RequiredArgsConstructor
@Slf4j
public class StripeService implements PaymentServiceInterface {
    
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        // Stripe-specific implementation
    }
    
    @Override
    public PaymentResponse executePayment(String paymentId, String payerId) {
        // Stripe-specific implementation
    }
    
    @Override
    public Payment getPaymentDetails(String paymentId) {
        // Stripe-specific implementation
    }
}
```

### Step 2: Create Configuration File
Create `src/main/resources/application-stripe.yml`:
```yaml
spring:
  profiles:
    active: stripe

stripe:
  api-key: ${STRIPE_API_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  return-url: http://localhost:8080/api/payment/success
  cancel-url: http://localhost:8080/api/payment/cancel
```

### Step 3: Add Dependencies (if needed)
Update `pom.xml`:
```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.0.0</version>
</dependency>
```

### Step 4: Test
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=stripe
```

## Configuration Files

### application.yml (Default)
Default configuration, usually activates `mock` profile

### application-mock.yml
Mock service configuration for testing

### application.yml (without profile suffix)
Production PayPal configuration

### application-applepay.yml
Apple Pay configuration

## Testing Strategy

### Unit Tests
Mock the `PaymentServiceInterface` in your tests:
```java
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    @Mock
    private PaymentServiceInterface paymentService;
    
    @InjectMocks
    private PaymentController paymentController;
    
    @Test
    void testCreatePayment() {
        // Test implementation
    }
}
```

### Integration Tests
Use the mock profile for integration tests:
```java
@SpringBootTest
@ActiveProfiles("mock")
class PaymentIntegrationTest {
    @Autowired
    private PaymentController paymentController;
    
    @Test
    void testPaymentFlow() {
        // Test full payment flow with mock service
    }
}
```

## Environment Variables

### PayPal (Production)
```bash
export PAYPAL_CLIENT_ID=your-client-id
export PAYPAL_CLIENT_SECRET=your-client-secret
export PAYPAL_MODE=sandbox  # or 'live'
```

### Apple Pay
```bash
export APPLEPAY_MERCHANT_ID=merchant.com.example
export APPLEPAY_PROCESSOR_API_KEY=your-processor-key
```

## Troubleshooting

### Issue: Wrong service implementation being used
**Solution**: Check that the correct profile is active:
```bash
# Check logs for active profile
tail -f logs/application.log | grep "active profiles"
```

### Issue: Dependency injection fails
**Solution**: Ensure only one implementation is available for the active profile. Use `@Profile` annotations correctly.

### Issue: Configuration not loading
**Solution**: Verify `application-{profile}.yml` exists and profile name matches:
```bash
ls -la src/main/resources/application-*.yml
```

## Future Enhancements

### 1. Generic Payment Details Object
Replace PayPal's `Payment` object with a generic `PaymentDetails` class:
```java
public interface PaymentServiceInterface {
    PaymentDetails getPaymentDetails(String paymentId);  // Instead of Payment
}
```

### 2. Multiple Providers Simultaneously
Support multiple payment providers at the same time using qualifiers:
```java
@RestController
public class PaymentController {
    private final PaymentServiceInterface payPalService;
    private final PaymentServiceInterface applePayService;
    
    @Autowired
    public PaymentController(
        @Qualifier("payPalService") PaymentServiceInterface payPalService,
        @Qualifier("applePayService") PaymentServiceInterface applePayService
    ) {
        this.payPalService = payPalService;
        this.applePayService = applePayService;
    }
}
```

### 3. Payment Provider Factory
Create a factory to select provider at runtime:
```java
@Service
public class PaymentProviderFactory {
    public PaymentServiceInterface getProvider(String providerType) {
        // Return appropriate provider based on type
    }
}
```

### 4. Provider-Specific Endpoints
```java
@PostMapping("/paypal/create")
public ResponseEntity<PaymentResponse> createPayPalPayment(...) {}

@PostMapping("/applepay/create")
public ResponseEntity<PaymentResponse> createApplePayment(...) {}
```

## Support

For issues or questions:
1. Check the logs in `logs/application.log`
2. Verify configuration in `application-{profile}.yml`
3. Ensure correct profile is active
4. Check that all required dependencies are in `pom.xml`

