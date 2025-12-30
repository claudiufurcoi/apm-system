# Mock Mode Guide

## Overview

This guide explains how to use the Mock Mode feature to develop and test the PayPal integration without requiring actual PayPal credentials or a PayPal sandbox account.

## What is Mock Mode?

Mock Mode is a development feature that simulates PayPal API interactions locally. It provides:

- ✅ **No PayPal credentials required** - Works out of the box
- ✅ **Instant testing** - No need to wait for PayPal sandbox setup
- ✅ **Predictable behavior** - All payments succeed by default
- ✅ **Full API compatibility** - Same endpoints and responses as production
- ✅ **Easy debugging** - Mock operations are clearly logged with 🎭 emoji

## Architecture

The application uses Spring Profiles to switch between real and mock PayPal implementations:

```
┌─────────────────────────────────────┐
│    PayPalServiceInterface           │
│  (Common contract for both modes)   │
└─────────────┬───────────────────────┘
              │
              ├──── @Profile("!mock")
              │     PayPalService (Real PayPal API)
              │
              └──── @Profile("mock")
                    MockPayPalService (Local simulation)
```

### Key Components

1. **PayPalServiceInterface** - Common interface for both implementations
2. **PayPalService** - Real implementation (active when NOT using mock profile)
3. **MockPayPalService** - Mock implementation (active with mock profile)
4. **PayPalConfig** - Real PayPal configuration (disabled in mock mode)
5. **MockPayPalConfig** - Mock configuration (provides dummy APIContext)

## How to Use Mock Mode

### Starting the Application

**Option 1: Maven command line**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

**Option 2: Environment variable**
```bash
export SPRING_PROFILES_ACTIVE=mock
mvn spring-boot:run
```

**Option 3: IDE (IntelliJ IDEA / Eclipse)**
- Add VM option: `-Dspring.profiles.active=mock`
- Or set environment variable: `SPRING_PROFILES_ACTIVE=mock`

### Testing the Mock Implementation

#### 1. Create a Mock Payment

```bash
curl -X POST http://localhost:8080/api/payment/create \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "test@example.com",
    "description": "Test Payment",
    "amount": 10.00,
    "currency": "USD",
    "orderId": "TEST-001"
  }'
```

**Response:**
```json
{
  "paymentId": "MOCK-PAY-D50A30F9",
  "status": "created",
  "approvalUrl": "http://localhost:8080/mock-paypal-approval?token=MOCK-TOKEN-C49D32D3&paymentId=MOCK-PAY-D50A30F9",
  "message": "Payment created successfully. Please redirect to PayPal for approval.",
  "orderId": "TEST-001"
}
```

#### 2. Simulate Payment Approval

Instead of redirecting to PayPal, directly call the success endpoint with the mock payment ID:

```bash
curl "http://localhost:8080/api/payment/success?paymentId=MOCK-PAY-D50A30F9&PayerID=MOCK-PAYER-123"
```

**Response:**
```json
{
  "paymentId": "MOCK-PAY-D50A30F9",
  "status": "approved",
  "approvalUrl": null,
  "message": "Payment completed successfully.",
  "orderId": null
}
```

#### 3. Get Payment Details

```bash
curl "http://localhost:8080/api/payment/MOCK-PAY-D50A30F9"
```

This returns a complete mock Payment object with all fields populated.

## Mock Behavior

### Payment IDs
- Format: `MOCK-PAY-{8 random chars}`
- Example: `MOCK-PAY-D50A30F9`

### Tokens
- Format: `MOCK-TOKEN-{8 random chars}`
- Example: `MOCK-TOKEN-C49D32D3`

### Payer IDs
- Always: `MOCK-PAYER-123`

### Payment States
- All payments automatically succeed
- State is always: `approved`

### Log Messages
All mock operations are logged with the 🎭 emoji prefix for easy identification:

```
🎭 MOCK: Creating PayPal payment for user: test@example.com, amount: 10.00 USD
🎭 MOCK: Payment created successfully. Payment ID: MOCK-PAY-D50A30F9
🎭 MOCK: Executing PayPal payment. Payment ID: MOCK-PAY-D50A30F9, Payer ID: MOCK-PAYER-123
🎭 MOCK: Payment executed successfully. State: approved
```

## Configuration Files

### application-mock.yml

This profile-specific configuration is automatically loaded when mock profile is active:

```yaml
spring:
  application:
    name: apm-service

paypal:
  mode: sandbox
  client-id: mock-client-id
  client-secret: mock-client-secret
  return-url: http://localhost:8080/api/payment/success
  cancel-url: http://localhost:8080/api/payment/cancel
```

Note: These credentials are dummy values and are not actually used by the mock implementation.

## Switching to Production Mode

To switch back to using real PayPal API:

1. **Remove the mock profile**:
   ```bash
   mvn spring-boot:run
   # Or
   export SPRING_PROFILES_ACTIVE=prod
   mvn spring-boot:run
   ```

2. **Set real PayPal credentials**:
   ```bash
   export PAYPAL_CLIENT_ID=your-real-client-id
   export PAYPAL_CLIENT_SECRET=your-real-client-secret
   ```

3. **Update application.yml** with real credentials

## Use Cases

### When to Use Mock Mode

✅ **Local development** - No external dependencies  
✅ **Unit/Integration testing** - Predictable, fast tests  
✅ **CI/CD pipelines** - No need for test credentials  
✅ **Demo/Presentations** - Works without internet  
✅ **Learning/Prototyping** - Understand the flow first  

### When to Use Production Mode

✅ **Testing actual PayPal integration**  
✅ **Verifying real payment flows**  
✅ **Testing with PayPal sandbox environment**  
✅ **Production deployments**  

## Troubleshooting

### Issue: Bean not found error

**Error:**
```
No qualifying bean of type 'com.apm.poc.service.PayPalService' available
```

**Solution:** Make sure you're running with the mock profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

### Issue: Application tries to connect to PayPal

**Solution:** Verify the mock profile is active. Check the startup logs for:
```
The following 1 profile is active: "mock"
🎭 MOCK MODE ENABLED: Using mock PayPal configuration
```

### Issue: Real PayPal errors when using mock mode

**Solution:** Both PayPalConfig and PayPalService should be excluded with `@Profile("!mock")`. Verify the annotations are present.

## Implementation Details

### MockPayPalService Methods

#### createPayment()
- Generates random mock payment ID
- Creates mock approval URL
- Returns success response immediately
- No external API calls

#### executePayment()
- Validates mock payment ID format
- Always returns approved status
- No external API calls

#### getPaymentDetails()
- Creates complete mock Payment object
- Includes payer info, transactions, amounts
- Returns realistic test data

## Benefits

1. **Development Speed** - Start coding immediately without PayPal setup
2. **Reliability** - No dependency on external services
3. **Cost** - No risk of accidentally creating real transactions
4. **Privacy** - No need to share credentials with team
5. **Offline Work** - Works without internet connection
6. **Testing** - Predictable behavior for automated tests

## Limitations

⚠️ **What Mock Mode Does NOT Test:**
- Real PayPal authentication flows
- PayPal-specific errors and edge cases
- Network latency and timeouts
- PayPal sandbox-specific behaviors
- Webhook callbacks from PayPal
- Currency conversion rates
- PayPal-side validation rules

For comprehensive testing, use PayPal Sandbox after initial development with Mock Mode.

## Next Steps

After developing with Mock Mode:

1. ✅ Get PayPal Sandbox credentials
2. ✅ Test with PayPal Sandbox (mode: sandbox)
3. ✅ Handle real PayPal errors and edge cases
4. ✅ Test with different currencies and amounts
5. ✅ Implement webhook handling
6. ✅ Get production credentials
7. ✅ Deploy with production mode (mode: live)

## Support

For questions or issues with Mock Mode:
- Check the main [README.md](../README.md)
- Review the source code in `MockPayPalService.java`
- Enable debug logging: `logging.level.com.apm.poc: DEBUG`

