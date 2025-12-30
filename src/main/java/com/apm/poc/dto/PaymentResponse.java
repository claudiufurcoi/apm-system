package com.apm.poc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String status; // created, approved, failed
    private String approvalUrl; // URL to redirect user to PayPal
    private String message;
    private String orderId;

    public static PaymentResponse success(String paymentId, String approvalUrl, String orderId) {
        return PaymentResponse.builder()
                .paymentId(paymentId)
                .status("created")
                .approvalUrl(approvalUrl)
                .message("Payment created successfully. Please redirect to PayPal for approval.")
                .orderId(orderId)
                .build();
    }

    public static PaymentResponse approved(String paymentId, String orderId) {
        return PaymentResponse.builder()
                .paymentId(paymentId)
                .status("approved")
                .message("Payment completed successfully.")
                .orderId(orderId)
                .build();
    }

    public static PaymentResponse failed(String message) {
        return PaymentResponse.builder()
                .status("failed")
                .message(message)
                .build();
    }

    public static PaymentResponse cancelled() {
        return PaymentResponse.builder()
                .status("cancelled")
                .message("Payment was cancelled by user.")
                .build();
    }
}

