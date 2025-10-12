package com.example.purchasedemo.service;

import com.example.purchasedemo.entity.Order;
import com.example.purchasedemo.entity.OrderStatus;
import com.example.purchasedemo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays; // Arrays import 추가
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbandonedOrderCleanupService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    // 30분마다 실행 (밀리초 단위)
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void cleanupAbandonedOrders() {
        log.info("Abandoned order cleanup job started.");

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        // PENDING과 PROCESSING 상태의 주문을 모두 조회하도록 변경
        List<Order> abandonedOrders = orderRepository.findByStatusInAndOrderDateBefore(
                Arrays.asList(OrderStatus.PENDING, OrderStatus.PROCESSING), thirtyMinutesAgo);

        if (abandonedOrders.isEmpty()) {
            log.info("No abandoned orders found.");
            return;
        }

        for (Order order : abandonedOrders) {
            log.info("Cleaning up abandoned order: ID={}, Product={}, Quantity={}, Status={}",
                    order.getId(), order.getProduct().getName(), order.getQuantity(), order.getStatus());

            // 주문 상태를 FAILED로 변경
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            // 재고 복구
            try {
                productService.increaseStock(order.getProduct().getId(), order.getQuantity());
                log.info("Stock recovered for product ID {} (quantity {}).", order.getProduct().getId(), order.getQuantity());
            } catch (Exception e) {
                log.error("Failed to recover stock for abandoned order ID {}: {}", order.getId(), e.getMessage());
                // TODO: 재고 복구 실패 시 알림 또는 재시도 로직 추가
            }
        }
        log.info("Abandoned order cleanup job finished. {} orders processed.", abandonedOrders.size());
    }
}
