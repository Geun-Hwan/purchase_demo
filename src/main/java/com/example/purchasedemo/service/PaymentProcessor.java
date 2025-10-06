package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.PaymentRequestMessage;
import com.example.purchasedemo.dto.PaymentResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    @Qualifier("paymentResultKafkaTemplate") // 이름 변경
    private final KafkaTemplate<String, PaymentResultMessage> paymentResultKafkaTemplate; // 이름 변경
    private final OrderService orderService;
    private final ProductService productService;
    private final Random random = new Random();

    @KafkaListener(topics = "payment-requests", groupId = "purchase_demo_group", containerFactory = "paymentRequestKafkaListenerContainerFactory") // 이름
                                                                                                                                                   // 변경
    @Transactional
    public void processPaymentRequest(PaymentRequestMessage message) {
        System.out.println("Received payment request: " + message);

        PaymentResultMessage resultMessage;
        /* TODO : 주문 및 결제로직 */
        if (random.nextBoolean()) { // Simulate 50% success rate
            orderService.updateOrderStatus(message.getOrderId(), "COMPLETED");
            resultMessage = new PaymentResultMessage(message.getOrderId(), "SUCCESS",
                    "Payment processed successfully.");
        } else {
            orderService.updateOrderStatus(message.getOrderId(), "FAILED");
            productService.increaseStock(message.getProductId(), message.getQuantity()); // Recover stock
            resultMessage = new PaymentResultMessage(message.getOrderId(), "FAILED",
                    "Payment failed due to an unknown error.");
        }

        paymentResultKafkaTemplate.send("payment-results", resultMessage); // 이름 변경
        System.out.println("Sent payment result: " + resultMessage);
    }
}
