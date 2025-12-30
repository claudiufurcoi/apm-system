package com.apm.poc.controller;

import com.apm.poc.dto.PaymentRequest;
import com.apm.poc.dto.PaymentResponse;
import com.apm.poc.service.PaymentServiceInterface;
import com.paypal.api.payments.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentController {

    private final PaymentServiceInterface paymentService;

    /**
     * Initiates a payment (PayPal, Apple Pay, etc.)
     * This endpoint is called when user clicks payment button
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request from: {}", request.getUserEmail());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Success callback - Payment provider redirects here after user approves payment
     */
    @GetMapping("/success")
    public ResponseEntity<PaymentResponse> paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {

        log.info("Payment success callback - Payment ID: {}, Payer ID: {}", paymentId, payerId);
        PaymentResponse response = paymentService.executePayment(paymentId, payerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel callback - Payment provider redirects here if user cancels payment
     */
    @GetMapping("/cancel")
    public ResponseEntity<PaymentResponse> paymentCancel() {
        log.info("Payment was cancelled by user");
        return ResponseEntity.ok(PaymentResponse.cancelled());
    }

    /**
     * Get payment details by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentDetails(@PathVariable String paymentId) {
        log.info("Fetching payment details for: {}", paymentId);
        Payment payment = paymentService.getPaymentDetails(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment service is running");
    }
}

