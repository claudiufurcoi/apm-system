package com.apm.poc.service;

import com.apm.poc.dto.PaymentRequest;
import com.apm.poc.dto.PaymentResponse;
import com.paypal.api.payments.Payment;

/**
 * Generic service interface for payment operations across different payment providers
 * Can be implemented by PayPal, Apple Pay, Stripe, or any other payment provider
 */
public interface PaymentServiceInterface {

    /**
     * Creates a payment and returns the approval/authorization URL
     */
    PaymentResponse createPayment(PaymentRequest request);

    /**
     * Executes the payment after user approval/authorization
     */
    PaymentResponse executePayment(String paymentId, String payerId);

    /**
     * Gets payment details
     * Note: Return type should be made generic in future refactoring
     * to support different payment provider response types
     */
    Payment getPaymentDetails(String paymentId);
}

