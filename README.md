# APM (Alternative Payment Method) - PayPal Integration

A Spring Boot service that integrates with PayPal for payment processing.

## Features

- Create PayPal payment
- Authenticate with PayPal
- Execute payment after user approval
- Handle success/cancel callbacks
- RESTful API endpoints
- **Multi-provider support** (PayPal, Apple Pay, etc.)
- **Mock service** for testing without credentials
- **Docker support** for easy deployment

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- PayPal Developer Account (optional - use mock mode for testing)
- **Docker & Docker Compose** (optional - for containerized deployment)

## Quick Start

### 🐳 Option 1: Run with Docker (Recommended)

The easiest way to run the application:

```bash
# Build the Docker image
docker-compose build apm-mock

# Start the service
docker-compose up apm-mock

# Access at http://localhost:8080
```

✅ **No Maven or Java installation required!**  
✅ **Works on Mac, Linux, Windows**  
✅ **Production-ready configuration**

**See [DOCKER_QUICKSTART.md](DOCKER_QUICKSTART.md) for complete Docker guide.**

### 💻 Option 2: Run with Maven

If you don't have PayPal credentials or want to test locally without PayPal API calls, you can use **Mock Mode**:

```bash
# Build the project
mvn clean install

# Run with mock profile
mvn spring-boot:run -Dspring-boot.run.profiles=mock

# Or set environment variable
export SPRING_PROFILES_ACTIVE=mock
mvn spring-boot:run
```

In mock mode:
- ✅ No PayPal credentials required
- ✅ All PayPal API calls are simulated locally
- ✅ Generates mock payment IDs and approval URLs
- ✅ Perfect for development and testing
- ✅ All endpoints work exactly the same way

**Testing in Mock Mode:**
```bash
# Create a mock payment
curl -X POST http://localhost:8080/api/payment/create \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "test@example.com",
    "description": "Test Payment",
    "amount": 10.00,
    "currency": "USD",
    "orderId": "TEST-001"
  }'

# The response will include a mock payment ID like: MOCK-PAY-A1B2C3D4
# To simulate payment approval, directly call the success endpoint:
curl "http://localhost:8080/api/payment/success?paymentId=MOCK-PAY-A1B2C3D4&PayerID=MOCK-PAYER-123"
```

### Option 2: Use Real PayPal API

### 1. Get PayPal Credentials

1. Go to [PayPal Developer Dashboard](https://developer.paypal.com/)
2. Create a new app or use an existing one
3. Get your **Client ID** and **Client Secret**

### 2. Configure Application

Update `src/main/resources/application.yml` with your PayPal credentials:

```yaml
paypal:
  mode: sandbox  # Use 'sandbox' for testing, 'live' for production
  client-id: YOUR_CLIENT_ID
  client-secret: YOUR_CLIENT_SECRET
```

Or set environment variables:
```bash
export PAYPAL_CLIENT_ID=your-client-id
export PAYPAL_CLIENT_SECRET=your-client-secret
```

### 3. Build and Run with Real PayPal

```bash
# Build the project
mvn clean install

# Run with default profile (uses real PayPal API)
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

### Switching Between Mock and Real PayPal

The application uses Spring Profiles to switch between mock and real implementations:

- **Mock Mode**: `--spring.profiles.active=mock` (no PayPal credentials needed)
- **Production Mode**: No profile or `--spring.profiles.active=prod` (requires PayPal credentials)

You can also set this in your IDE's run configuration or in `application.yml`.

## API Endpoints

### 1. Create Payment (Initiate PayPal Payment)

**Endpoint:** `POST /api/payment/create`

**Request Body:**
```json
{
  "userEmail": "user@example.com",
  "description": "Order #12345",
  "amount": 99.99,
  "currency": "USD",
  "orderId": "ORDER-12345"
}
```

**Response:**
```json
{
  "paymentId": "PAYID-XXXXX",
  "status": "created",
  "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=XXXXX",
  "message": "Payment created successfully. Please redirect to PayPal for approval.",
  "orderId": "ORDER-12345"
}
```

**Usage:**
1. Your frontend calls this endpoint when user clicks "Pay with PayPal"
2. Get the `approvalUrl` from response
3. Redirect user to this URL
4. User logs in to PayPal and approves payment
5. PayPal redirects back to your success URL

### 2. Payment Success (Callback from PayPal)

**Endpoint:** `GET /api/payment/success?paymentId={id}&PayerID={payerId}`

PayPal automatically redirects to this endpoint after user approves payment.

**Response:**
```json
{
  "paymentId": "PAYID-XXXXX",
  "status": "approved",
  "message": "Payment completed successfully."
}
```

### 3. Payment Cancel (Callback from PayPal)

**Endpoint:** `GET /api/payment/cancel`

PayPal redirects here if user cancels payment.

### 4. Get Payment Details

**Endpoint:** `GET /api/payment/{paymentId}`

Get details of a specific payment.

### 5. Health Check

**Endpoint:** `GET /api/payment/health`

Check if service is running.

## Payment Flow

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Client    │         │  APM Service │         │   PayPal    │
│  (Browser)  │         │   (Backend)  │         │             │
└─────────────┘         └──────────────┘         └─────────────┘
      │                         │                        │
      │  1. Click "Pay"        │                        │
      │───────────────────────>│                        │
      │                         │                        │
      │                         │  2. Create Payment    │
      │                         │───────────────────────>│
      │                         │                        │
      │                         │  3. Return approval URL│
      │                         │<───────────────────────│
      │                         │                        │
      │  4. Redirect to PayPal │                        │
      │<────────────────────────│                        │
      │                         │                        │
      │  5. User logs in & approves                     │
      │────────────────────────────────────────────────>│
      │                         │                        │
      │  6. Redirect to success URL                     │
      │<────────────────────────────────────────────────│
      │                         │                        │
      │  7. Execute payment    │                        │
      │───────────────────────>│                        │
      │                         │  8. Execute payment   │
      │                         │───────────────────────>│
      │                         │                        │
      │                         │  9. Payment confirmed │
      │                         │<───────────────────────│
      │                         │                        │
      │  10. Show success      │                        │
      │<────────────────────────│                        │
```

## Testing with cURL

```bash
# Create a payment
curl -X POST http://localhost:8080/api/payment/create \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "test@example.com",
    "description": "Test Payment",
    "amount": 10.00,
    "currency": "USD",
    "orderId": "TEST-001"
  }'

# Copy the approvalUrl from response and open in browser
# After approval, PayPal will redirect to success URL

# Check payment details
curl http://localhost:8080/api/payment/{paymentId}
```

## Frontend Integration Example

```javascript
// When user clicks "Pay with PayPal" button
async function payWithPayPal() {
  const paymentData = {
    userEmail: document.getElementById('email').value,
    description: 'Product Purchase',
    amount: 99.99,
    currency: 'USD',
    orderId: 'ORDER-' + Date.now()
  };

  try {
    const response = await fetch('http://localhost:8080/api/payment/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(paymentData)
    });

    const result = await response.json();
    
    if (result.approvalUrl) {
      // Redirect to PayPal
      window.location.href = result.approvalUrl;
    }
  } catch (error) {
    console.error('Payment error:', error);
  }
}
```

## Project Structure

```
apm/
├── src/
│   ├── main/
│   │   ├── java/com/apm/poc/
│   │   │   ├── ApmApplication.java         # Main application class
│   │   │   ├── config/
│   │   │   │   └── PayPalConfig.java       # PayPal configuration
│   │   │   ├── controller/
│   │   │   │   └── PaymentController.java  # REST API endpoints
│   │   │   ├── service/
│   │   │   │   └── PayPalService.java      # Payment business logic
│   │   │   ├── dto/
│   │   │   │   ├── PaymentRequest.java     # Payment request DTO
│   │   │   │   └── PaymentResponse.java    # Payment response DTO
│   │   │   └── exception/
│   │   │       ├── PaymentException.java   # Custom exception
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.yml             # Configuration
│   └── test/
└── pom.xml
```

## Technologies Used

- Spring Boot 2.7.18
- PayPal REST API SDK 1.14.0
- Java 11
- Maven
- Lombok

## Error Handling

The service includes comprehensive error handling:
- Validation errors for invalid input
- PayPal API errors
- Network errors
- General exceptions

All errors return proper HTTP status codes and meaningful messages.

## Security Notes

⚠️ **Important for Production:**
1. Never commit credentials to version control
2. Use environment variables or secret management
3. Enable HTTPS/SSL
4. Implement authentication/authorization
5. Add rate limiting
6. Validate all inputs
7. Use `mode: live` only in production

## Support

For issues or questions:
- PayPal Developer: https://developer.paypal.com/
- PayPal API Reference: https://developer.paypal.com/docs/api/

## License

MIT

