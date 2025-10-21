package com.example.purchasemanager.notification.kakao.service;

import com.example.purchasemanager.notification.kakao.dto.KakaoMessageDto;
import com.example.purchasemanager.core.service.notification.NotificationInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoNotificationService implements NotificationInterface {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.api.url}")
    private String kakaoApiUrl;

    @Value("${kakao.api.token}")
    private String kakaoApiToken;

    @Value("${kakao.link.web-url}")
    private String webUrl;

    @Value("${kakao.link.mobile-web-url}")
    private String mobileWebUrl;

    @Override
    public void sendPaymentSuccessNotification(Long orderId, String message) {
        String notificationText = String.format("결제가 성공적으로 완료되었습니다.\n\n- 주문 ID: %d\n- 메시지: %s",
                orderId, message);
        sendKakaoTalkNotification(notificationText, orderId);
    }

    @Override
    public void sendPaymentFailureNotification(Long orderId, String message) {
        String notificationText = String.format("결제가 실패했습니다.\n\n- 주문 ID: %d\n- 메시지: %s",
                orderId, message);
        sendKakaoTalkNotification(notificationText, orderId);
    }

    private void sendKakaoTalkNotification(String notificationText, Long orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(kakaoApiToken);

        KakaoMessageDto.Link link = new KakaoMessageDto.Link(webUrl, mobileWebUrl);
        KakaoMessageDto.Default templateDto = new KakaoMessageDto.Default(
                "text",
                notificationText,
                link,
                "주문내역 확인");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        try {
            String templateJson = objectMapper.writeValueAsString(templateDto);
            log.info("전송할 template_object JSON: {}", templateJson);
            body.add("template_object", templateJson);
        } catch (Exception e) {
            log.error("카카오톡 메시지 JSON 변환 실패", e);
            return;
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(kakaoApiUrl, requestEntity, String.class);
            log.info("카카오톡 알림 전송 성공: 주문 ID {}", orderId);
        } catch (Exception e) {
            log.error("카카오톡 알림 전송 실패: {}", e.getMessage());
        }
    }
}