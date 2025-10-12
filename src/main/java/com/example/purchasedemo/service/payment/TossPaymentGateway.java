package com.example.purchasedemo.service.payment;

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
public class TossPaymentGateway implements PaymentGateway {

    @Value("${TOSS_SECRET_KEY}")
    private String secretKey;

    @Value("${toss.api.url}")
    private String tossApiUrl;

    @Value("${toss.redirect.base-url}")
    private String redirectBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public TossPaymentResponse requestPayment(Order order) {
        HttpHeaders headers = createHeaders();
        TossPaymentRequest requestDto = new TossPaymentRequest(
                "카드",
                order.getTotalPrice().longValue(),
                order.getId().toString() + "_" + System.currentTimeMillis(),
                order.getProduct().getName(),
                redirectBaseUrl + "/api/v1/payments/success",
                redirectBaseUrl + "/api/v1/payments/fail"
        );

        HttpEntity<TossPaymentRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        TossPaymentResponse response = restTemplate.postForEntity(
                tossApiUrl,
                requestEntity,
                TossPaymentResponse.class).getBody();

        if (response == null || !"READY".equals(response.getStatus())) {
            throw new IllegalStateException("Failed to create Toss payment");
        }

        return response;
    }

    @Override
    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount) {
        HttpHeaders headers = createHeaders();
        TossPaymentConfirmRequestDto requestDto = new TossPaymentConfirmRequestDto(orderId, amount);

        HttpEntity<TossPaymentConfirmRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

        return Objects.requireNonNull(restTemplate.postForEntity(
                tossApiUrl + "/" + paymentKey,
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
