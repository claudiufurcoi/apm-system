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
 * Mock implementation of PayPalService for testing without PayPal credentials
 */
@Service("payPalService")
@Profile("mock")
@Slf4j
public class MockPayPalService implements PaymentServiceInterface {

    @Value("${paypal.return-url:http://localhost:8080/api/payment/success}")
    private String returnUrl;

    @Value("${paypal.cancel-url:http://localhost:8080/api/payment/cancel}")
    private String cancelUrl;

    /**
     * Creates a mock PayPal payment response
     */
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("🎭 MOCK: Creating PayPal payment for user: {}, amount: {} {}",
                request.getUserEmail(), request.getAmount(), request.getCurrency());

        try {
            // Validate input
            if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
                throw new PaymentException("Invalid amount");
            }

            // Generate mock payment ID
            String mockPaymentId = "MOCK-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Generate mock approval URL
            String mockToken = "MOCK-TOKEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String approvalUrl = "http://localhost:8080/mock-paypal-approval?token=" + mockToken +
                               "&paymentId=" + mockPaymentId;

            log.info("🎭 MOCK: Payment created successfully. Payment ID: {}", mockPaymentId);
            log.info("🎭 MOCK: Approval URL: {}", approvalUrl);
            log.info("🎭 MOCK: In a real scenario, you would redirect to PayPal. " +
                    "For testing, you can directly call the success endpoint:");
            log.info("🎭 MOCK: GET {}?paymentId={}&PayerID=MOCK-PAYER-123", returnUrl, mockPaymentId);

            return PaymentResponse.success(
                    mockPaymentId,
                    approvalUrl,
                    request.getOrderId()
            );

        } catch (Exception e) {
            log.error("🎭 MOCK: Error creating mock payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create mock payment: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the mock payment
     */
    public PaymentResponse executePayment(String paymentId, String payerId) {
        log.info("🎭 MOCK: Executing PayPal payment. Payment ID: {}, Payer ID: {}", paymentId, payerId);

        try {
            // Validate mock payment ID format
            if (!paymentId.startsWith("MOCK-PAY-")) {
                throw new PaymentException("Invalid mock payment ID format");
            }

            log.info("🎭 MOCK: Payment executed successfully. State: approved");

            return PaymentResponse.approved(paymentId, null);

        } catch (Exception e) {
            log.error("🎭 MOCK: Error executing mock payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to execute mock payment: " + e.getMessage(), e);
        }
    }

    /**
     * Gets mock payment details
     */
    public Payment getPaymentDetails(String paymentId) {
        log.info("🎭 MOCK: Fetching payment details for payment ID: {}", paymentId);

        try {
            // Create a mock Payment object
            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setState("approved");
            payment.setIntent("sale");
            payment.setCreateTime("2025-12-29T10:00:00Z");
            payment.setUpdateTime("2025-12-29T10:01:00Z");

            // Create mock payer
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");
            payer.setStatus("VERIFIED");

            PayerInfo payerInfo = new PayerInfo();
            payerInfo.setEmail("mock-user@example.com");
            payerInfo.setFirstName("Mock");
            payerInfo.setLastName("User");
            payerInfo.setPayerId("MOCK-PAYER-123");
            payer.setPayerInfo(payerInfo);
            payment.setPayer(payer);

            // Create mock transaction
            Transaction transaction = new Transaction();
            Amount amount = new Amount();
            amount.setCurrency("USD");
            amount.setTotal("10.00");
            transaction.setAmount(amount);
            transaction.setDescription("Mock payment transaction");

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);
            payment.setTransactions(transactions);

            // Create mock links
            Links approvalLink = new Links();
            approvalLink.setHref("http://localhost:8080/mock-paypal-approval?token=MOCK-TOKEN");
            approvalLink.setRel("approval_url");
            approvalLink.setMethod("REDIRECT");

            Links selfLink = new Links();
            selfLink.setHref("http://localhost:8080/api/payment/" + paymentId);
            selfLink.setRel("self");
            selfLink.setMethod("GET");

            List<Links> links = new ArrayList<>();
            links.add(approvalLink);
            links.add(selfLink);
            payment.setLinks(links);

            log.info("🎭 MOCK: Payment details retrieved. State: approved");
            return payment;

        } catch (Exception e) {
            log.error("🎭 MOCK: Failed to get payment details: {}", e.getMessage(), e);
            throw new PaymentException("Failed to get mock payment details: " + e.getMessage(), e);
        }
    }
}

