package com.example.purchasedemo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.purchasemanager.core.entity.Order;
import com.example.purchasemanager.core.entity.User;
import com.example.purchasemanager.core.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
        Optional<List<Order>> findByUser(User user);

        Page<Order> findByUser(User user, Pageable pageable);

        Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

        Page<Order> findByUserAndOrderDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate,
                        Pageable pageable);

        Page<Order> findByUserAndStatusAndOrderDateBetween(User user, OrderStatus status, LocalDateTime startDate,
                        LocalDateTime endDate, Pageable pageable);

        List<Order> findByStatusAndOrderDateBefore(OrderStatus status, LocalDateTime orderDate);

        // 새로운 쿼리 메서드 추가
        List<Order> findByStatusInAndOrderDateBefore(List<OrderStatus> statuses, LocalDateTime orderDate);
}
