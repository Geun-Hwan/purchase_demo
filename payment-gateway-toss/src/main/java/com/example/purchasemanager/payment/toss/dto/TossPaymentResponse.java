package com.example.purchasemanager.payment.toss.dto; // Changed package

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TossPaymentResponse {
    private String status;
    private String paymentKey;

    @JsonProperty("checkout")
    private Checkout checkout;

    @Data
    public static class Checkout {
        private String url;
    }

    // 편의 메소드
    public String getCheckoutUrl() {
        return this.checkout != null ? this.checkout.getUrl() : null;
    }
}
