package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.PaymentResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    @KafkaListener(topics = "payment-results", groupId = "purchase_demo_group", containerFactory = "paymentResultKafkaListenerContainerFactory") // 이름 변경
    public void sendPaymentNotification(PaymentResultMessage message) {
        System.out.println("Received payment result for notification: " + message);

        String notificationMessage;
        if ("SUCCESS".equals(message.getStatus())) {
            notificationMessage = String.format("결제가 성공적으로 완료되었습니다. 주문 ID: %d, 메시지: %s", message.getOrderId(), message.getMessage());
        } else {
            notificationMessage = String.format("결제가 실패했습니다. 주문 ID: %d, 메시지: %s", message.getOrderId(), message.getMessage());
        }

        // Simulate sending alarm talk (print to console)
        System.out.println("알림톡 발송: " + notificationMessage);
    }
}
