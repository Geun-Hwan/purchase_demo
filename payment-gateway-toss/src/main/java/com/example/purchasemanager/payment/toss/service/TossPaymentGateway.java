package com.example.purchasemanager.payment.toss.service; // Changed package

import com.example.purchasemanager.core.dto.PaymentResponse;
import com.example.purchasemanager.core.entity.Order;
import com.example.purchasemanager.core.service.payment.PaymentGateway;
import com.example.purchasemanager.payment.toss.dto.TossPaymentConfirmRequestDto;
import com.example.purchasemanager.payment.toss.dto.TossPaymentRequest;
import com.example.purchasemanager.payment.toss.dto.TossPaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Objects;

@Service("tossPaymentGateway")
public class TossPaymentGateway implements PaymentGateway {

    @Value("${TOSS_SECRET_KEY}")
    private String secretKey;

    @Value("${toss.api.url}")
    private String tossApiUrl;

    @Value("${toss.redirect.base-url}")
    private String redirectBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public PaymentResponse requestPayment(Order order) { // Changed return type
        HttpHeaders headers = createHeaders();
        TossPaymentRequest requestDto = new TossPaymentRequest(
                "카드",
                order.getTotalPrice().longValue(),
                order.getId().toString() + "_" + System.currentTimeMillis(),
                order.getProduct().getName(),
                redirectBaseUrl + "/api/v1/payments/success",
                redirectBaseUrl + "/api/v1/payments/fail");

        HttpEntity<TossPaymentRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        TossPaymentResponse tossResponse = restTemplate.postForEntity( // Changed variable name
                tossApiUrl,
                requestEntity,
                TossPaymentResponse.class).getBody();

        if (tossResponse == null || !"READY".equals(tossResponse.getStatus())) {
            throw new IllegalStateException("Failed to create Toss payment");
        }

        return PaymentResponse.builder() // Mapped to generic PaymentResponse
                .paymentId(tossResponse.getPaymentKey())
                .status(tossResponse.getStatus())
                .checkoutUrl(tossResponse.getCheckoutUrl())
                .orderId(order.getId().toString())
                .amount(order.getTotalPrice().longValue())
                .message("Toss payment initiated")
                .build();
    }

    @Override
    public PaymentResponse confirmPayment(String paymentKey, String orderId, Long amount) { // Changed return type
        HttpHeaders headers = createHeaders();
        TossPaymentConfirmRequestDto requestDto = new TossPaymentConfirmRequestDto(orderId, amount);

        HttpEntity<TossPaymentConfirmRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

        TossPaymentResponse tossResponse = Objects.requireNonNull(restTemplate.postForEntity( // Changed variable name
                tossApiUrl + "/" + paymentKey,
                requestEntity,
                TossPaymentResponse.class).getBody());

        return PaymentResponse.builder() // Mapped to generic PaymentResponse
                .paymentId(tossResponse.getPaymentKey())
                .status(tossResponse.getStatus())
                .orderId(orderId)
                .amount(amount)
                .message("Toss payment confirmed")
                .build();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String encodedSecretKey = Base64.getEncoder().encodeToString((this.secretKey + ":").getBytes());
        headers.setBasicAuth(encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
