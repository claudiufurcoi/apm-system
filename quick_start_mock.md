# Quick Start - Mock Mode

## 🚀 Get Started in 2 Minutes

No PayPal account? No problem! Run the app in mock mode:

```bash
# 1. Build the project
mvn clean install

# 2. Run with mock profile
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

## ✅ Test It

**Create a mock payment:**
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

You'll get a response with a mock payment ID like:
```json
{
  "paymentId": "MOCK-PAY-D50A30F9",
  "status": "created",
  "approvalUrl": "http://localhost:8080/mock-paypal-approval?token=...",
  "message": "Payment created successfully. Please redirect to PayPal for approval.",
  "orderId": "TEST-001"
}
```

**Simulate approval:**
```bash
curl "http://localhost:8080/api/payment/success?paymentId=MOCK-PAY-D50A30F9&PayerID=MOCK-PAYER-123"
```

You'll get:
```json
{
  "paymentId": "MOCK-PAY-D50A30F9",
  "status": "approved",
  "message": "Payment completed successfully."
}
```

## 🎯 What You Get

- ✅ No PayPal credentials needed
- ✅ All endpoints work the same way
- ✅ Perfect for local development
- ✅ Great for testing and demos
- ✅ Mock operations logged with 🎭 emoji

## 📖 Need More Info?

- Full details: [MOCK_MODE_GUIDE.md](mock_guide.md)
- Real PayPal setup: [README.md](README.md)

## 🔄 Switch to Real PayPal

When ready, just remove the mock profile and set real credentials:

```bash
export PAYPAL_CLIENT_ID=your-client-id
export PAYPAL_CLIENT_SECRET=your-client-secret
mvn spring-boot:run
```

