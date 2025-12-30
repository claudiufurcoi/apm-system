package com.apm.poc.service;

import com.apm.poc.config.PayPalConfig;
import com.apm.poc.dto.PaymentRequest;
import com.apm.poc.dto.PaymentResponse;
import com.apm.poc.exception.PaymentException;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service("payPalService")
@Profile("!mock & !mock-applepay")
@RequiredArgsConstructor
@Slf4j
public class PayPalService implements PaymentServiceInterface {

    private final APIContext apiContext;

    @Value("${paypal.return-url:http://localhost:8080/api/payment/success}")
    private String returnUrl;

    @Value("${paypal.cancel-url:http://localhost:8080/api/payment/cancel}")
    private String cancelUrl;

    /**
     * Creates a PayPal payment and returns the approval URL
     */
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating PayPal payment for user: {}, amount: {} {}",
                request.getUserEmail(), request.getAmount(), request.getCurrency());

        try {
            // Create amount
            Amount amount = new Amount();
            amount.setCurrency(request.getCurrency());
            amount.setTotal(formatAmount(request.getAmount()));

            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setDescription(request.getDescription());
            transaction.setAmount(amount);

            // Add transaction to list
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            // Create payer
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            // Set payer info (optional but recommended)
            PayerInfo payerInfo = new PayerInfo();
            payerInfo.setEmail(request.getUserEmail());
            payer.setPayerInfo(payerInfo);

            // Create payment
            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            // Set redirect URLs
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl(cancelUrl);
            redirectUrls.setReturnUrl(returnUrl);
            payment.setRedirectUrls(redirectUrls);

            // Create payment on PayPal
            Payment createdPayment = payment.create(apiContext);
            log.info("Payment created successfully. Payment ID: {}", createdPayment.getId());

            // Extract approval URL
            String approvalUrl = extractApprovalUrl(createdPayment);

            return PaymentResponse.success(
                    createdPayment.getId(),
                    approvalUrl,
                    request.getOrderId()
            );

        } catch (PayPalRESTException e) {
            log.error("PayPal REST API error: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create PayPal payment: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the payment after user approval
     */
    public PaymentResponse executePayment(String paymentId, String payerId) {
        log.info("Executing PayPal payment. Payment ID: {}, Payer ID: {}", paymentId, payerId);

        try {
            Payment payment = new Payment();
            payment.setId(paymentId);

            PaymentExecution paymentExecute = new PaymentExecution();
            paymentExecute.setPayerId(payerId);

            Payment executedPayment = payment.execute(apiContext, paymentExecute);
            log.info("Payment executed successfully. State: {}", executedPayment.getState());

            if ("approved".equals(executedPayment.getState())) {
                return PaymentResponse.approved(executedPayment.getId(), null);
            } else {
                return PaymentResponse.failed("Payment not approved. State: " + executedPayment.getState());
            }

        } catch (PayPalRESTException e) {
            log.error("PayPal REST API error during execution: {}", e.getMessage(), e);
            throw new PaymentException("Failed to execute PayPal payment: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error executing payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to execute payment: " + e.getMessage(), e);
        }
    }

    /**
     * Gets payment details
     */
    public Payment getPaymentDetails(String paymentId) {
        log.info("Fetching payment details for payment ID: {}", paymentId);

        try {
            Payment payment = Payment.get(apiContext, paymentId);
            log.info("Payment details retrieved. State: {}", payment.getState());
            return payment;
        } catch (PayPalRESTException e) {
            log.error("Failed to get payment details: {}", e.getMessage(), e);
            throw new PaymentException("Failed to get payment details: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the approval URL from the payment links
     */
    private String extractApprovalUrl(Payment payment) {
        for (Links link : payment.getLinks()) {
            if ("approval_url".equals(link.getRel())) {
                return link.getHref();
            }
        }
        throw new PaymentException("No approval URL found in PayPal response");
    }

    /**
     * Formats amount to 2 decimal places
     */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
}

