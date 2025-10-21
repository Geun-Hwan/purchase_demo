package com.example.purchasedemo.service;

import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.purchasedemo.config.KafkaTopics;
import com.example.purchasedemo.exception.PaymentException;
import com.example.purchasedemo.repository.OrderRepository;
import com.example.purchasemanager.core.dto.PaymentResultMessage;
import com.example.purchasemanager.core.entity.Order;
import com.example.purchasemanager.core.enums.OrderStatus;
import com.example.purchasemanager.core.enums.PaymentGatewayType;
import com.example.purchasemanager.core.service.payment.PaymentGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessor {

    private final KafkaTemplate<String, PaymentResultMessage> paymentResultKafkaTemplate;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final Map<String, PaymentGateway> paymentGatewayMap; // Map으로 변경

    @Transactional
    public Long processPaymentConfirmation(PaymentGatewayType gatewayType, String paymentKey, String orderId,
            Long amount) {
        Long originalOrderId = parseOriginalOrderId(orderId);

        PaymentGateway paymentGateway = paymentGatewayMap.get(gatewayType.name().toLowerCase() + "PaymentGateway");
        if (paymentGateway == null) {
            throw new IllegalArgumentException("Unsupported payment gateway type: " + gatewayType);
        }

        try {
            paymentGateway.confirmPayment(paymentKey, orderId, amount);
        } catch (Exception e) {
            handlePaymentFailure(originalOrderId, gatewayType.name() + " Payments 승인 실패: " + e.getMessage());
            throw new PaymentException(gatewayType.name() + " Payments 승인에 실패했습니다.", e);
        }

        handlePaymentSuccess(originalOrderId);
        return originalOrderId;
    }

    @Transactional
    public void processPaymentFailure(PaymentGatewayType gatewayType, String orderId, String message) {
        Long originalOrderId = parseOriginalOrderId(orderId);
        handlePaymentFailure(originalOrderId, gatewayType.name() + " Payments 결제 실패: " + message);
    }

    @Transactional
    public void processPaymentCancellation(PaymentGatewayType gatewayType, String orderId, String message) {
        Long originalOrderId = parseOriginalOrderId(orderId);
        // CANCELLED 상태로 직접 처리
        Order order = findOrderById(originalOrderId);
        if (OrderStatus.COMPLETED.equals(order.getStatus())) {
            // 이미 결제 완료된 주문을 취소하는 경우 (환불 로직 필요)
            // 여기서는 간단히 CANCELLED로 변경하고 재고 복구
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            productService.increaseStock(order.getProduct().getId(), order.getQuantity());
            log.info("결제 완료된 주문(ID: {})이 사용자 요청으로 취소되었습니다. 재고 복구 완료.", originalOrderId);
        } else if (OrderStatus.PENDING.equals(order.getStatus()) || OrderStatus.PROCESSING.equals(order.getStatus())) {
            // 결제 대기 또는 진행 중인 주문을 취소하는 경우
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            productService.increaseStock(order.getProduct().getId(), order.getQuantity());
            log.info("결제 대기/진행 중인 주문(ID: {})이 사용자 요청으로 취소되었습니다. 재고 복구 완료.", originalOrderId);
        } else {
            log.warn("이미 처리된 주문(ID: {})에 대한 중복 취소 요청입니다. 현재 상태: {}", originalOrderId, order.getStatus());
        }

        PaymentResultMessage resultMessage = new PaymentResultMessage(order.getId(), OrderStatus.CANCELLED,
                gatewayType.name() + " 결제가 사용자 요청으로 취소되었습니다: " + message);
        paymentResultKafkaTemplate.send(KafkaTopics.PAYMENT_RESULTS, resultMessage);
        log.info("결제 취소 메시지 발행: {}", resultMessage);
    }

    private void handlePaymentSuccess(Long orderId) {
        Order order = findOrderById(orderId);
        if (!OrderStatus.PENDING.equals(order.getStatus()) && !OrderStatus.PROCESSING.equals(order.getStatus())) { // PROCESSING
                                                                                                                   // 상태도
                                                                                                                   // 추가
            log.warn("이미 처리된 주문(ID: {})에 대한 중복 성공 요청입니다.", orderId);
            return;
        }
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        PaymentResultMessage resultMessage = new PaymentResultMessage(order.getId(), OrderStatus.COMPLETED,
                "결제가 성공적으로 완료되었습니다.");
        paymentResultKafkaTemplate.send(KafkaTopics.PAYMENT_RESULTS, resultMessage);
        log.info("결제 성공 메시지 발행: {}", resultMessage);
    }

    public void handlePaymentFailure(Long orderId, String failureMessage) {
        try {
            Order order = findOrderById(orderId);
            if (!OrderStatus.PENDING.equals(order.getStatus()) && !OrderStatus.PROCESSING.equals(order.getStatus())) { // PROCESSING
                                                                                                                       // 상태도
                                                                                                                       // 추가
                log.warn("이미 처리된 주문(ID: {})에 대한 중복 실패 요청입니다.", orderId);
                return;
            }
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            productService.increaseStock(order.getProduct().getId(), order.getQuantity());

            PaymentResultMessage resultMessage = new PaymentResultMessage(order.getId(), OrderStatus.FAILED,
                    failureMessage);
            paymentResultKafkaTemplate.send(KafkaTopics.PAYMENT_RESULTS, resultMessage);
            log.info("결제 실패 메시지 발행: {}", resultMessage);
        } catch (Exception e) {
            log.error("결제 실패 후속 처리 중 오류 발생: Order ID - {}", orderId, e);
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
