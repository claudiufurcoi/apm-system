# 🐳 Docker Guide for APM Payment Service

## 📋 Overview

This guide explains how to run the APM payment service using Docker instead of `mvn spring-boot:run`.

## 🚀 Quick Start

### 1. Build and Run with Mock Service (No credentials needed!)

```bash
# Build the Docker image
docker-compose build

# Start the mock service
docker-compose up apm-mock

# Or run in detached mode
docker-compose up -d apm-mock
```

The application will be available at: **http://localhost:8080**

### 2. Test the Application

```bash
# Health check
curl http://localhost:8080/api/payment/health

# Create a test payment
curl -X POST http://localhost:8080/api/payment/create \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "USD",
    "description": "Test payment",
    "orderID": "ORDER-123",
    "userEmail": "test@example.com"
  }'
```

### 3. Stop the Application

```bash
# Stop the service
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## 🔧 Configuration Options

### Running Different Payment Providers

#### Mock Service (Default - No Credentials)
```bash
docker-compose up apm-mock
```
- Port: **8080**
- Profile: `mock`
- No credentials needed ✅

#### PayPal Service (Production)
```bash
# 1. Create .env file from template
cp .env.example .env

# 2. Edit .env and add your PayPal credentials
nano .env

# 3. Uncomment apm-paypal service in docker-compose.yml

# 4. Start the service
docker-compose up apm-paypal
```
- Port: **8081**
- Profile: `prod`
- Requires PayPal credentials

#### Apple Pay Service
```bash
# 1. Configure .env with Apple Pay credentials

# 2. Uncomment apm-applepay service in docker-compose.yml

# 3. Start the service
docker-compose up apm-applepay
```
- Port: **8082**
- Profile: `applepay`
- Requires Apple Pay credentials

## 📦 Docker Commands Reference

### Build Commands

```bash
# Build the image
docker-compose build

# Build without cache (fresh build)
docker-compose build --no-cache

# Build specific service
docker-compose build apm-mock
```

### Run Commands

```bash
# Start service in foreground (see logs)
docker-compose up apm-mock

# Start service in background (detached)
docker-compose up -d apm-mock

# Start all services
docker-compose up -d

# Start with specific environment variables
docker-compose up -d -e SPRING_PROFILES_ACTIVE=mock
```

### Management Commands

```bash
# View running containers
docker-compose ps

# View logs
docker-compose logs apm-mock

# Follow logs (real-time)
docker-compose logs -f apm-mock

# View last 100 lines
docker-compose logs --tail=100 apm-mock

# Restart service
docker-compose restart apm-mock

# Stop service
docker-compose stop apm-mock

# Remove containers
docker-compose down

# Remove containers and volumes
docker-compose down -v

# Remove containers, volumes, and images
docker-compose down -v --rmi all
```

### Debugging Commands

```bash
# Execute commands inside running container
docker-compose exec apm-mock sh

# View container details
docker inspect apm-mock

# View container stats (CPU, memory)
docker stats apm-mock

# Check health status
docker inspect --format='{{.State.Health.Status}}' apm-mock
```

## 🏗️ Docker Architecture

### Multi-Stage Build
```
┌─────────────────────────────────────┐
│  Stage 1: Build (maven:3.8.6)       │
│  - Download dependencies            │
│  - Compile source code              │
│  - Package as JAR                   │
└──────────────┬──────────────────────┘
               │ Copy JAR
               ↓
┌─────────────────────────────────────┐
│  Stage 2: Runtime (JRE 11 Alpine)   │
│  - Minimal size (~150MB)            │
│  - Only JRE + JAR                   │
│  - Non-root user (security)         │
└─────────────────────────────────────┘
```

### Container Features

✅ **Multi-stage build** - Smaller final image (~150MB vs ~600MB)  
✅ **Security** - Runs as non-root user  
✅ **Health checks** - Automatic health monitoring  
✅ **Optimized JVM** - Container-aware memory settings  
✅ **Fast startup** - Optimized for containerized environment  
✅ **Layer caching** - Dependencies cached separately  

## 📊 Port Mapping

| Service | Container Port | Host Port | Profile |
|---------|---------------|-----------|---------|
| apm-mock | 8080 | 8080 | mock |
| apm-paypal | 8080 | 8081 | prod |
| apm-applepay | 8080 | 8082 | applepay |

## 🔐 Environment Variables

### Available Environment Variables

```bash
# Spring Profile Selection
SPRING_PROFILES_ACTIVE=mock|prod|applepay

# PayPal Configuration
PAYPAL_MODE=sandbox|live
PAYPAL_CLIENT_ID=your-client-id
PAYPAL_CLIENT_SECRET=your-client-secret

# Apple Pay Configuration
APPLEPAY_MERCHANT_ID=merchant.com.example
APPLEPAY_PROCESSOR_API_KEY=your-api-key

# JVM Configuration
JAVA_OPTS=-Xms256m -Xmx512m
```

### Setting Environment Variables

#### Method 1: .env file (Recommended)
```bash
# Create .env file
cp .env.example .env

# Edit with your values
nano .env
```

#### Method 2: Command line
```bash
PAYPAL_CLIENT_ID=xxx PAYPAL_CLIENT_SECRET=yyy docker-compose up apm-paypal
```

#### Method 3: docker-compose.yml
Edit the `environment` section in `docker-compose.yml`

## 🔍 Troubleshooting

### Container won't start
```bash
# Check logs
docker-compose logs apm-mock

# Check if port is already in use
lsof -i :8080

# Rebuild without cache
docker-compose build --no-cache
```

### Health check failing
```bash
# Check health status
docker inspect --format='{{.State.Health.Status}}' apm-mock

# Check if application is responding
docker-compose exec apm-mock wget -O- http://localhost:8080/api/payment/health
```

### Out of memory
```bash
# Increase memory limit in docker-compose.yml
environment:
  - JAVA_OPTS=-Xms512m -Xmx1024m
```

### Permission denied errors
```bash
# Check if running as non-root user (should be)
docker-compose exec apm-mock whoami
# Output should be: spring

# If needed, rebuild image
docker-compose build --no-cache
```

## 📈 Performance Tuning

### JVM Memory Settings
```yaml
environment:
  # For development (low memory)
  - JAVA_OPTS=-Xms128m -Xmx256m
  
  # For production (more memory)
  - JAVA_OPTS=-Xms512m -Xmx1024m
  
  # For high load (maximum memory)
  - JAVA_OPTS=-Xms1024m -Xmx2048m
```

### Resource Limits
Add to docker-compose.yml:
```yaml
services:
  apm-mock:
    # ... existing config ...
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

## 🔄 CI/CD Integration

### GitHub Actions Example
```yaml
name: Build and Push Docker Image

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Build Docker image
        run: docker-compose build
      
      - name: Test application
        run: |
          docker-compose up -d apm-mock
          sleep 30
          curl -f http://localhost:8080/api/payment/health
```

## 📚 Comparison: Docker vs Maven

| Feature | mvn spring-boot:run | Docker |
|---------|---------------------|--------|
| Setup Time | Fast (if Maven installed) | Slower (first build) |
| Subsequent Runs | Fast | Very Fast |
| Isolation | ❌ Uses host environment | ✅ Isolated container |
| Portability | ⚠️ Requires Maven + Java | ✅ Runs anywhere |
| Production Ready | ❌ Not recommended | ✅ Production ready |
| Resource Usage | ⚠️ Uses host resources | ✅ Limited resources |
| Multi-instance | ⚠️ Difficult | ✅ Easy |
| CI/CD Integration | ⚠️ Requires tooling | ✅ Standard |

## 🎯 Best Practices

### Development
```bash
# Use mock service for rapid development
docker-compose up apm-mock

# Use volume mounts for hot reload (advanced)
# Add to docker-compose.yml:
volumes:
  - ./src:/app/src
```

### Testing
```bash
# Run tests in container
docker-compose run --rm apm-mock mvn test

# Run with specific profile
docker-compose run --rm -e SPRING_PROFILES_ACTIVE=test apm-mock
```

### Production
```bash
# Use specific version tags
docker tag apm:latest apm:v1.0.0

# Use health checks
# Already configured in docker-compose.yml

# Use restart policies
# Already configured: restart: unless-stopped

# Monitor logs
docker-compose logs -f --tail=100 apm-mock
```

## 🆚 Docker vs mvn spring-boot:run

### Use Docker When:
✅ Deploying to production  
✅ Need consistent environment  
✅ Running multiple instances  
✅ CI/CD pipeline  
✅ Team collaboration  

### Use Maven When:
✅ Active development with hot reload  
✅ Quick local testing  
✅ Debugging with IDE  
✅ Running tests frequently  

## 📝 Quick Reference Card

```bash
# BUILD
docker-compose build                    # Build image

# START
docker-compose up apm-mock             # Start mock service
docker-compose up -d apm-mock          # Start in background

# LOGS
docker-compose logs -f apm-mock        # Follow logs

# TEST
curl http://localhost:8080/api/payment/health

# STOP
docker-compose down                     # Stop all services

# CLEAN
docker-compose down -v --rmi all       # Remove everything
```

## 🎉 Summary

You can now run the APM payment service using Docker:

```bash
# Instead of:
mvn spring-boot:run -Dspring-boot.run.profiles=mock

# Use:
docker-compose up apm-mock
```

**Benefits:**
- ✅ Consistent environment
- ✅ Easy deployment
- ✅ Production-ready
- ✅ Isolated from host
- ✅ Works on any platform

---

**Created**: December 29, 2025  
**Status**: ✅ Ready to Use  
**Next**: Run `docker-compose up apm-mock` to start!

