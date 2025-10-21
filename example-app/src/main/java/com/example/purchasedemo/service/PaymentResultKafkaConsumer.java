package com.example.purchasedemo.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.purchasedemo.config.KafkaTopics;
import com.example.purchasemanager.core.dto.PaymentResultMessage;
import com.example.purchasemanager.core.enums.OrderStatus;
import com.example.purchasemanager.core.service.notification.NotificationInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentResultKafkaConsumer {

    private final NotificationInterface notificationInterface; // notificationInterface 인터페이스 주입

    @KafkaListener(topics = KafkaTopics.PAYMENT_RESULTS, groupId = "purchase_demo_group", containerFactory = "paymentResultKafkaListenerContainerFactory")
    public void handlePaymentResult(PaymentResultMessage message) {
        log.info("Received payment result from Kafka: {}", message);

        if (OrderStatus.COMPLETED.equals(message.getStatus())) { // "SUCCESS" -> OrderStatus.COMPLETED로 변경
            notificationInterface.sendPaymentSuccessNotification(message.getOrderId(), message.getMessage());
        } else {
            notificationInterface.sendPaymentFailureNotification(message.getOrderId(), message.getMessage());
        }
    }
}
