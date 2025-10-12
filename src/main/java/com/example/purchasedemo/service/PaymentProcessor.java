package com.example.purchasedemo.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.purchasedemo.dto.PaymentResultMessage;
import com.example.purchasedemo.entity.Order;
import com.example.purchasedemo.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    private final KafkaTemplate<String, PaymentResultMessage> paymentResultKafkaTemplate;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final TossPaymentService tossPaymentService;

    @Transactional
    public void processTossPaymentConfirmation(String paymentKey, String orderId, Long amount) {
        // 1. Toss Payments에 결제 승인 요청
        try {
            tossPaymentService.confirmPayment(paymentKey, orderId, amount);
        } catch (Exception e) {
            // 결제 승인 실패 시 처리
            handlePaymentFailure(orderId, e.getMessage());
            throw new RuntimeException("Toss Payments 승인 실패: " + e.getMessage(), e);
        }

        // 2. 결제 승인 성공 시 후속 처리
        handlePaymentSuccess(orderId);
    }

    private void handlePaymentSuccess(String tossOrderId) {
        Order order = findOrderByTossOrderId(tossOrderId);
        order.setStatus("PAID");
        orderRepository.save(order);

        PaymentResultMessage resultMessage = new PaymentResultMessage(order.getId(), "SUCCESS",
                "결제가 성공적으로 완료되었습니다.");
        paymentResultKafkaTemplate.send("payment-results", resultMessage);
        System.out.println("결제 성공 메시지 발행: " + resultMessage);
    }

    private void handlePaymentFailure(String tossOrderId, String failureMessage) {
        try {
            Order order = findOrderByTossOrderId(tossOrderId);
            order.setStatus("FAILED");
            orderRepository.save(order);

            // 재고 원복
            productService.increaseStock(order.getProduct().getId(), order.getQuantity());

            PaymentResultMessage resultMessage = new PaymentResultMessage(order.getId(), "FAILED",
                    "결제 실패: " + failureMessage);
            paymentResultKafkaTemplate.send("payment-results", resultMessage);
            System.out.println("결제 실패 메시지 발행: " + resultMessage);
        } catch (Exception e) {
            // 주문 정보를 찾지 못하는 등 후속 처리 실패 시 로깅
            System.err.println("결제 실패 후속 처리 중 오류 발생: " + e.getMessage());
        }
    }

    private Order findOrderByTossOrderId(String tossOrderId) {
        // TossOrderId 형식: "{orderId}_{timestamp}"
        Long originalOrderId = Long.parseLong(tossOrderId.split("_")[0]);
        return orderRepository.findById(originalOrderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + originalOrderId));
    }
}
