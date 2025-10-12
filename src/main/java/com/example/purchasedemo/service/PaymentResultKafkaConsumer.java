package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.PaymentResultMessage;
import com.example.purchasedemo.entity.OrderStatus; // OrderStatus import 추가
import com.example.purchasedemo.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentResultKafkaConsumer {

    private final NotificationService notificationService; // NotificationService 인터페이스 주입

    @KafkaListener(topics = "payment-results", groupId = "purchase_demo_group", containerFactory = "paymentResultKafkaListenerContainerFactory")
    public void handlePaymentResult(PaymentResultMessage message) {
        log.info("Received payment result from Kafka: {}", message);

        if (OrderStatus.COMPLETED.equals(message.getStatus())) { // "SUCCESS" -> OrderStatus.COMPLETED로 변경
            notificationService.sendPaymentSuccessNotification(message.getOrderId(), message.getMessage());
        } else {
            notificationService.sendPaymentFailureNotification(message.getOrderId(), message.getMessage());
        }
    }
}
