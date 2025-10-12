package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.KakaoMessageDto;
import com.example.purchasedemo.dto.PaymentResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.api.url}")
    private String kakaoApiUrl;

    @Value("${kakao.api.token}")
    private String kakaoApiToken;

    @KafkaListener(topics = "payment-results", groupId = "purchase_demo_group", containerFactory = "paymentResultKafkaListenerContainerFactory")
    public void handlePaymentResult(PaymentResultMessage message) {
        if ("SUCCESS".equals(message.getStatus())) {
            sendKakaoTalkNotification(message);
        } else {
            log.error("결제 실패 알림 수신: {}", message);
        }
    }

    private void sendKakaoTalkNotification(PaymentResultMessage message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(kakaoApiToken);

        String notificationText = String.format("결제가 성공적으로 완료되었습니다.\n\n- 주문 ID: %d\n- 메시지: %s",
                message.getOrderId(), message.getMessage());

        KakaoMessageDto.Default templateDto = new KakaoMessageDto.Default(
                "text",
                notificationText,
                new KakaoMessageDto.Link(),
                "주문내역 확인"
        );

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        try {
            String templateJson = objectMapper.writeValueAsString(templateDto);
            log.info("전송할 template_object JSON: {}", templateJson); // 로그 추가
            body.add("template_object", templateJson);
        } catch (Exception e) {
            log.error("카카오톡 메시지 JSON 변환 실패", e);
            return;
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(kakaoApiUrl, requestEntity, String.class);
            log.info("카카오톡 알림 전송 성공: 주문 ID {}", message.getOrderId());
        } catch (Exception e) {
            log.error("카카오톡 알림 전송 실패: {}", e.getMessage());
            // TODO: 로깅 및 재시도 로직 등 예외 처리 강화
        }
    }
}
