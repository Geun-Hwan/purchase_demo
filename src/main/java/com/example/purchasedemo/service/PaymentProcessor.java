package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.PaymentResultMessage;
import com.example.purchasedemo.entity.Order;
import com.example.purchasedemo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    private final KafkaTemplate<String, PaymentResultMessage> paymentResultKafkaTemplate;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final TossPaymentService tossPaymentService;

    @Transactional
    public Long processTossPaymentConfirmation(String paymentKey, String orderId, Long amount) {
        Long originalOrderId = parseOriginalOrderId(orderId);

        try {
            tossPaymentService.confirmPayment(paymentKey, orderId, amount);
        } catch (Exception e) {
            handlePaymentFailure(originalOrderId, "Toss Payments 승인 실패: " + e.getMessage());
            throw new RuntimeException("Toss Payments 승인 실패: " + e.getMessage(), e);
        }

        handlePaymentSuccess(originalOrderId);
        return originalOrderId;
    }

    @Transactional
    public void processTossPaymentCancellation(String orderId, String message) {
        Long originalOrderId = parseOriginalOrderId(orderId);
        handlePaymentFailure(originalOrderId, "사용자 취소 또는 결제 실패: " + message);
    }

    private void handlePaymentSuccess(Long orderId) {
        Order order = findOrderById(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            // 이미 처리된 주문이면 중복 처리 방지
            return;
        }
        order.setStatus("PAID");
        orderRepository.save(order);

        PaymentResultMessage resultMessage = new PaymentResultMessage(order.getId(), "SUCCESS",
                "결제가 성공적으로 완료되었습니다.");
        paymentResultKafkaTemplate.send("payment-results", resultMessage);
        System.out.println("결제 성공 메시지 발행: " + resultMessage);
    }

    private void handlePaymentFailure(Long orderId, String failureMessage) {
        try {
            Order order = findOrderById(orderId);
            if (!"PENDING".equals(order.getStatus())) {
                // 이미 처리된 주문이면 중복 처리 방지
                return;
            }
            order.setStatus("FAILED");
            orderRepository.save(order);

            productService.increaseStock(order.getProduct().getId(), order.getQuantity());

            PaymentResultMessage resultMessage = new PaymentResultMessage(order.getId(), "FAILED", failureMessage);
            paymentResultKafkaTemplate.send("payment-results", resultMessage);
            System.out.println("결제 실패 메시지 발행: " + resultMessage);
        } catch (Exception e) {
            System.err.println("결제 실패 후속 처리 중 오류 발생: " + e.getMessage());
        }
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
    }

    private Long parseOriginalOrderId(String tossOrderId) {
        return Long.parseLong(tossOrderId.split("_")[0]);
    }
}
