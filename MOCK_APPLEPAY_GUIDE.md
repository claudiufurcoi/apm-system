# 🍎 Mock Apple Pay Service Guide

## Overview

The Mock Apple Pay Service allows you to test Apple Pay payment flows **without any Apple Pay credentials or merchant account**. Perfect for development and testing!

## ✅ What's Included

- **MockApplePayService.java** - Complete mock implementation
- **application-mock-applepay.yml** - Mock configuration
- **Docker support** - Ready to run in container

## 🚀 Quick Start

### Option 1: Run with Maven

```bash
# Start the mock Apple Pay service
mvn spring-boot:run -Dspring-boot.run.profiles=mock-applepay

# Service starts on http://localhost:8080
```

### Option 2: Run with Docker

```bash
# Build the image (if not already built)
docker-compose build apm-mock-applepay

# Start the service
docker-compose up apm-mock-applepay

# Or in background
docker-compose up -d apm-mock-applepay
```

## 🧪 Testing the Mock Apple Pay Service

### 1. Create a Payment

```bash
curl -X POST http://localhost:8080/api/payment/create \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 99.99,
    "currency": "USD",
    "description": "Apple Watch Series 8",
    "orderID": "ORDER-AP-001",
    "userEmail": "customer@example.com"
  }'
```

**Response:**
```json
{
  "paymentId": "AP-MOCK-A1B2C3D4",
  "status": "created",
  "approvalUrl": "http://localhost:8080/mock-applepay-session?token=MOCK-AP-TOKEN-X1Y2Z3W4&transactionId=AP-MOCK-A1B2C3D4",
  "message": "Payment created successfully. Please redirect to Apple Pay for approval.",
  "orderId": "ORDER-AP-001"
}
```

### 2. Execute the Payment

In a real Apple Pay flow, the user would approve via Face ID/Touch ID and Apple would call your backend. For testing, call the success endpoint directly:

```bash
# Use the paymentId from step 1
curl "http://localhost:8080/api/payment/success?paymentId=AP-MOCK-A1B2C3D4&PayerID=MOCK-AP-PAYER-123"
```

**Response:**
```json
{
  "paymentId": "AP-MOCK-A1B2C3D4",
  "status": "approved",
  "message": "Payment completed successfully."
}
```

### 3. Get Payment Details

```bash
curl http://localhost:8080/api/payment/AP-MOCK-A1B2C3D4
```

## 🔍 What Gets Logged

The mock service provides detailed logging to help you understand the flow:

```
🍎 MOCK APPLE PAY: Creating payment for user: customer@example.com, amount: 99.99 USD
🍎 MOCK APPLE PAY: Payment session created successfully
🍎 MOCK APPLE PAY: Transaction ID: AP-MOCK-A1B2C3D4
🍎 MOCK APPLE PAY: Session URL: http://localhost:8080/mock-applepay-session?...
🍎 MOCK APPLE PAY: In a real scenario, the client would use Apple Pay JS to show payment sheet
🍎 MOCK APPLE PAY: For testing, you can directly call the success endpoint:
🍎 MOCK APPLE PAY: GET http://localhost:8080/api/payment/success?paymentId=AP-MOCK-A1B2C3D4&PayerID=MOCK-AP-PAYER-123
```

## 🎯 Mock Payment Flow

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   Client    │         │  APM Service     │         │  Mock Apple Pay │
│  (Browser)  │         │  (Backend)       │         │                 │
└─────────────┘         └──────────────────┘         └─────────────────┘
      │                         │                            │
      │  1. Create Payment     │                            │
      │───────────────────────>│                            │
      │                         │                            │
      │                         │  2. Generate Mock Session │
      │                         │───────────────────────────>│
      │                         │                            │
      │                         │  3. Return Mock Session   │
      │                         │<───────────────────────────│
      │                         │                            │
      │  4. Mock Session URL   │                            │
      │<────────────────────────│                            │
      │                         │                            │
      │  5. Simulate Approval  │                            │
      │  (Direct API Call)     │                            │
      │───────────────────────────────────────────────────>│
      │                         │                            │
      │  6. Payment Approved   │                            │
      │<───────────────────────────────────────────────────│
```

## 📊 Available Services & Ports

| Service | Port | Profile | Credentials | Command |
|---------|------|---------|-------------|---------|
| **Mock PayPal** | 8080 | `mock` | ❌ None | `docker-compose up apm-mock` |
| **Mock Apple Pay** | 8082 | `mock-applepay` | ❌ None | `docker-compose up apm-mock-applepay` |
| PayPal (Real) | 8081 | `prod` | ✅ Required | `docker-compose up apm-paypal` |
| Apple Pay (Real) | 8082 | `applepay` | ✅ Required | `docker-compose up apm-applepay` |

## 🔧 Configuration

### Mock Apple Pay Configuration (`application-mock-applepay.yml`)

```yaml
spring:
  profiles:
    active: mock-applepay

applepay:
  merchant-id: merchant.com.mock.apm
  merchant-name: APM Payment Demo (Mock)
  country-code: US
  currency-code: USD
  return-url: http://localhost:8080/api/payment/success
  cancel-url: http://localhost:8080/api/payment/cancel
  mock-mode: true
```

## 🆚 Mock vs Real Apple Pay

| Feature | Mock Apple Pay | Real Apple Pay |
|---------|----------------|----------------|
| Credentials | ❌ Not required | ✅ Merchant certificate required |
| Apple Developer Account | ❌ Not required | ✅ Required |
| Payment Processing | ✅ Simulated | ✅ Real money |
| Transaction ID Format | `AP-MOCK-XXXXXXXX` | Apple's format |
| Payment Sheet | ❌ Not shown | ✅ Native iOS/macOS UI |
| Face ID/Touch ID | ❌ Not used | ✅ Required for auth |
| Testing | ✅ Perfect for dev | ⚠️ Requires test cards |
| Setup Time | ⏱️ Instant | ⏱️ Days (approval needed) |

## 🎓 Understanding the Mock Implementation

### Key Differences from Real Apple Pay

1. **Transaction IDs**: Mock uses `AP-MOCK-` prefix
2. **No Encryption**: Real Apple Pay encrypts payment tokens
3. **No Certificate**: Mock doesn't validate merchant certificates
4. **Direct API**: Mock allows direct success endpoint calls
5. **No Native UI**: Mock doesn't show Apple Pay payment sheet

### What the Mock Simulates

✅ Payment session creation  
✅ Transaction ID generation  
✅ Payment approval flow  
✅ Payment execution  
✅ Transaction details retrieval  
✅ Success/cancel callbacks  

### What the Mock Doesn't Simulate

❌ Native Apple Pay UI (payment sheet)  
❌ Face ID / Touch ID authentication  
❌ Payment token encryption/decryption  
❌ Merchant certificate validation  
❌ Real payment processing  

## 🔄 Switching Between Services

### Run Multiple Services Simultaneously

```bash
# Mock PayPal on port 8080
docker-compose up -d apm-mock

# Mock Apple Pay on port 8082
docker-compose up -d apm-mock-applepay

# Now you have both running!
# PayPal:    http://localhost:8080
# Apple Pay: http://localhost:8082
```

### Switch Profiles with Maven

```bash
# Mock PayPal
mvn spring-boot:run -Dspring-boot.run.profiles=mock

# Mock Apple Pay
mvn spring-boot:run -Dspring-boot.run.profiles=mock-applepay

# Real PayPal
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Real Apple Pay
mvn spring-boot:run -Dspring-boot.run.profiles=applepay
```

## 📝 Complete Test Script

```bash
#!/bin/bash
# test-mock-applepay.sh

BASE_URL="http://localhost:8082"

echo "🍎 Testing Mock Apple Pay Service"
echo "=================================="

# 1. Health Check
echo "1. Health Check..."
curl -s "${BASE_URL}/api/payment/health"
echo -e "\n"

# 2. Create Payment
echo "2. Creating payment..."
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/payment/create" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 99.99,
    "currency": "USD",
    "description": "Test Product",
    "orderID": "ORDER-TEST-001",
    "userEmail": "test@example.com"
  }')

echo "$RESPONSE" | jq .
PAYMENT_ID=$(echo "$RESPONSE" | jq -r '.paymentId')
echo "Payment ID: $PAYMENT_ID"
echo ""

# 3. Execute Payment
echo "3. Executing payment..."
curl -s "${BASE_URL}/api/payment/success?paymentId=${PAYMENT_ID}&PayerID=MOCK-AP-PAYER-123" | jq .
echo ""

# 4. Get Payment Details
echo "4. Getting payment details..."
curl -s "${BASE_URL}/api/payment/${PAYMENT_ID}" | jq .
echo ""

echo "✅ Test completed!"
```

Run it:
```bash
chmod +x test-mock-applepay.sh
./test-mock-applepay.sh
```

## 🐛 Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose logs apm-mock-applepay

# Or with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=mock-applepay
```

### Port Already in Use

```bash
# Check what's using port 8082
lsof -i :8082

# Change port in docker-compose.yml
ports:
  - "8083:8080"  # Use 8083 instead
```

### Invalid Transaction ID Error

Make sure the transaction ID starts with `AP-MOCK-`:
```
✅ Correct:  AP-MOCK-A1B2C3D4
❌ Wrong:    MOCK-PAY-A1B2C3D4  (this is PayPal format)
```

## 🎯 Use Cases

### Development
- ✅ Test payment flows without credentials
- ✅ Rapid iteration and debugging
- ✅ No external dependencies
- ✅ Works offline

### Testing
- ✅ Automated integration tests
- ✅ CI/CD pipeline testing
- ✅ Load testing payment flows
- ✅ End-to-end testing

### Demos
- ✅ Show payment flows to stakeholders
- ✅ Customer demos without real accounts
- ✅ Training and onboarding

### Not Suitable For
- ❌ Production use
- ❌ Real payment processing
- ❌ Security audits
- ❌ Compliance testing

## 📚 Next Steps

### Ready for Real Apple Pay?

1. **Get Apple Developer Account**
   - Enroll at developer.apple.com
   - Pay $99/year fee

2. **Create Merchant ID**
   - Create in Apple Developer Portal
   - Format: `merchant.com.yourcompany.app`

3. **Get Payment Processor**
   - Stripe, Braintree, or similar
   - Integrate their Apple Pay SDK

4. **Update Configuration**
   - Switch profile from `mock-applepay` to `applepay`
   - Add real credentials to `.env`
   - Uncomment real service in `docker-compose.yml`

5. **Test with Real Cards**
   - Add test cards to Apple Wallet
   - Use sandbox environment first
   - Go live when ready

## 🎉 Summary

You now have a **fully functional Mock Apple Pay service** that:

- ✅ Requires **no credentials**
- ✅ Simulates complete payment flow
- ✅ Works with **Docker or Maven**
- ✅ Perfect for **development and testing**
- ✅ Easy to switch to real Apple Pay later

**Try it now:**
```bash
# Start the service
docker-compose up apm-mock-applepay

# Test it
curl http://localhost:8082/api/payment/health
```

---

**Created**: December 29, 2025  
**Status**: ✅ Complete and Tested  
**Profile**: `mock-applepay`  
**Port**: 8082

