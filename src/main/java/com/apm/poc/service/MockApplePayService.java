package com.apm.poc.service;

import com.apm.poc.dto.PaymentRequest;
import com.apm.poc.dto.PaymentResponse;
import com.apm.poc.exception.PaymentException;
import com.paypal.api.payments.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock implementation of Apple Pay Service for testing without Apple Pay credentials
 * This service simulates Apple Pay payment flow for development and testing purposes
 */
@Service("applePayService")
@Profile("mock-applepay")
@Slf4j
public class MockApplePayService implements PaymentServiceInterface {

    @Value("${applepay.return-url:http://localhost:8080/api/payment/success}")
    private String returnUrl;

    @Value("${applepay.cancel-url:http://localhost:8080/api/payment/cancel}")
    private String cancelUrl;

    /**
     * Creates a mock Apple Pay payment session
     */
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("🍎 MOCK APPLE PAY: Creating payment for user: {}, amount: {} {}",
                request.getUserEmail(), request.getAmount(), request.getCurrency());

        try {
            // Validate input
            if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
                throw new PaymentException("Invalid amount");
            }

            // Generate mock Apple Pay transaction ID
            String mockTransactionId = "AP-MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Generate mock Apple Pay payment session URL
            String mockSessionUrl = "http://localhost:8080/mock-applepay-session?token=MOCK-AP-TOKEN-" +
                                   UUID.randomUUID().toString().substring(0, 8).toUpperCase() +
                                   "&transactionId=" + mockTransactionId;

            log.info("🍎 MOCK APPLE PAY: Payment session created successfully");
            log.info("🍎 MOCK APPLE PAY: Transaction ID: {}", mockTransactionId);
            log.info("🍎 MOCK APPLE PAY: Session URL: {}", mockSessionUrl);
            log.info("🍎 MOCK APPLE PAY: In a real scenario, the client would use Apple Pay JS to show payment sheet");
            log.info("🍎 MOCK APPLE PAY: For testing, you can directly call the success endpoint:");
            log.info("🍎 MOCK APPLE PAY: GET {}?paymentId={}&PayerID=MOCK-AP-PAYER-123", returnUrl, mockTransactionId);

            return PaymentResponse.success(
                    mockTransactionId,
                    mockSessionUrl,
                    request.getOrderId()
            );

        } catch (Exception e) {
            log.error("🍎 MOCK APPLE PAY: Error creating mock payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create mock Apple Pay payment: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the mock Apple Pay payment
     */
    @Override
    public PaymentResponse executePayment(String paymentId, String paymentToken) {
        log.info("🍎 MOCK APPLE PAY: Processing payment. Transaction ID: {}, Token: {}", paymentId, paymentToken);

        try {
            // Validate mock transaction ID format
            if (!paymentId.startsWith("AP-MOCK-")) {
                throw new PaymentException("Invalid mock Apple Pay transaction ID format");
            }

            // Simulate payment processing delay
            log.info("🍎 MOCK APPLE PAY: Validating payment token...");
            log.info("🍎 MOCK APPLE PAY: Processing payment with merchant...");
            log.info("🍎 MOCK APPLE PAY: Payment authorized successfully");

            return PaymentResponse.approved(paymentId, null);

        } catch (Exception e) {
            log.error("🍎 MOCK APPLE PAY: Error processing mock payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to process mock Apple Pay payment: " + e.getMessage(), e);
        }
    }

    /**
     * Gets mock Apple Pay transaction details
     */
    @Override
    public Payment getPaymentDetails(String paymentId) {
        log.info("🍎 MOCK APPLE PAY: Fetching payment details for transaction ID: {}", paymentId);

        try {
            // Validate mock transaction ID format
            if (!paymentId.startsWith("AP-MOCK-")) {
                throw new PaymentException("Invalid mock Apple Pay transaction ID format");
            }

            // Create a mock Payment object
            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setState("approved");
            payment.setIntent("sale");

            // Add mock transaction details
            Transaction transaction = new Transaction();
            Amount amount = new Amount();
            amount.setCurrency("USD");
            amount.setTotal("100.00");
            transaction.setAmount(amount);
            transaction.setDescription("Mock Apple Pay Transaction");

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);
            payment.setTransactions(transactions);

            // Add mock payer info
            Payer payer = new Payer();
            payer.setPaymentMethod("apple_pay");
            PayerInfo payerInfo = new PayerInfo();
            payerInfo.setEmail("mock-applepay-user@example.com");
            payerInfo.setFirstName("Mock");
            payerInfo.setLastName("Apple Pay User");
            payer.setPayerInfo(payerInfo);
            payment.setPayer(payer);

            log.info("🍎 MOCK APPLE PAY: Payment details retrieved successfully. State: approved");
            return payment;

        } catch (Exception e) {
            log.error("🍎 MOCK APPLE PAY: Failed to get payment details: {}", e.getMessage(), e);
            throw new PaymentException("Failed to get mock Apple Pay payment details: " + e.getMessage(), e);
        }
    }

    /**
     * Simulates Apple Pay payment sheet display
     * In a real implementation, this would trigger the native Apple Pay UI
     */
    public String simulatePaymentSheet(PaymentRequest request) {
        log.info("🍎 MOCK APPLE PAY: Simulating Apple Pay payment sheet for amount: {} {}",
                request.getAmount(), request.getCurrency());

        return "Mock Apple Pay sheet would be displayed with:\n" +
               "- Merchant: APM Payment Demo\n" +
               "- Amount: " + request.getAmount() + " " + request.getCurrency() + "\n" +
               "- Description: " + request.getDescription() + "\n" +
               "- User can approve or cancel using Face ID/Touch ID";
    }
}

