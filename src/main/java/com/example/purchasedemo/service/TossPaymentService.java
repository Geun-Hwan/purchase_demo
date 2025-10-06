package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.TossPaymentConfirmRequestDto;
import com.example.purchasedemo.dto.TossPaymentRequest;
import com.example.purchasedemo.dto.TossPaymentResponse;
import com.example.purchasedemo.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Objects;

@Service
public class TossPaymentService {

    @Value("${TOSS_SECRET_KEY}")
    private String secretKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments";

    public TossPaymentResponse requestPayment(Order order) {
        HttpHeaders headers = createHeaders();
        TossPaymentRequest requestDto = new TossPaymentRequest(
                "카드", // 예시: 결제 수단
                order.getTotalPrice().longValue(),
                order.getId().toString(),
                order.getProduct().getName(),
                "http://localhost:8080/api/v1/payments/success", // 성공 URL
                "http://localhost:8080/api/v1/payments/fail" // 실패 URL
        );

        HttpEntity<TossPaymentRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        TossPaymentResponse response = restTemplate.postForEntity(
                TOSS_API_URL,
                requestEntity,
                TossPaymentResponse.class).getBody();

        if (response == null || !"READY".equals(response.getStatus())) {
            throw new IllegalStateException("Failed to create Toss payment");
        }

        return response;
    }

    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount) {
        HttpHeaders headers = createHeaders();
        TossPaymentConfirmRequestDto requestDto = new TossPaymentConfirmRequestDto(orderId, amount);

        HttpEntity<TossPaymentConfirmRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

        return Objects.requireNonNull(restTemplate.postForEntity(
                TOSS_API_URL + "/" + paymentKey,
                requestEntity,
                TossPaymentResponse.class).getBody());
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String encodedSecretKey = Base64.getEncoder().encodeToString((this.secretKey + ":").getBytes());
        headers.setBasicAuth(encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
