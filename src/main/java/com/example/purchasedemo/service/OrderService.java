package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.OrderRequest;
import com.example.purchasedemo.dto.OrderResponse;
import com.example.purchasedemo.dto.TossPaymentResponse;
import com.example.purchasedemo.entity.Order;
import com.example.purchasedemo.entity.OrderStatus;
import com.example.purchasedemo.entity.Product;
import com.example.purchasedemo.entity.User;
import com.example.purchasedemo.exception.OrderNotFoundException;
import com.example.purchasedemo.exception.PaymentException;
import com.example.purchasedemo.exception.UnauthorizedAccessException;
import com.example.purchasedemo.exception.UserNotFoundException;
import com.example.purchasedemo.repository.OrderRepository;
import com.example.purchasedemo.repository.UserRepository;
import com.example.purchasedemo.service.payment.PaymentGateway;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductService productService;
  private final PaymentGateway paymentGateway;
  private final RedissonClient redissonClient;

  @Transactional
  public OrderResponse createOrder(OrderRequest orderRequest) {
    String username = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
    User user = userRepository
        .findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

    RLock lock = redissonClient.getLock(
        "product_lock:" + orderRequest.getProductId());
    try {
      boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
      if (!isLocked) {
        throw new IllegalStateException(
            "Could not acquire lock for product " +
                orderRequest.getProductId());
      }

      Product product = productService.getProductEntityById(
          orderRequest.getProductId());

      productService.decreaseStock(
          product.getId(),
          orderRequest.getQuantity());

      Double totalPrice = product.getPrice() * orderRequest.getQuantity();

      Order order = Order
          .builder()
          .user(user)
          .product(product)
          .quantity(orderRequest.getQuantity())
          .totalPrice(totalPrice)
          .orderDate(LocalDateTime.now())
          .status(OrderStatus.PENDING) // 초기 상태는 PENDING
          .build();

      Order savedOrder = orderRepository.save(order);

      try {
          TossPaymentResponse tossPaymentResponse = paymentGateway.requestPayment(savedOrder); // 결제 요청
          String paymentUrl = tossPaymentResponse.getCheckoutUrl();

          // 결제 요청이 성공적으로 이루어져 paymentUrl을 받아온 직후 상태를 PROCESSING으로 변경
          savedOrder.setStatus(OrderStatus.PROCESSING);
          orderRepository.save(savedOrder); // 상태 업데이트

        return new OrderResponse(
            savedOrder.getId(),
            savedOrder.getUser().getUsername(),
            savedOrder.getProduct().getName(),
            savedOrder.getQuantity(),
            savedOrder.getTotalPrice(),
            savedOrder.getOrderDate(),
            savedOrder.getStatus(), // 현재 상태 (PROCESSING)
            paymentUrl);
      } catch (Exception e) {
        // 결제 요청 실패 시 주문 상태를 FAILED로 변경하고 재고 복구
        savedOrder.setStatus(OrderStatus.FAILED);
        orderRepository.save(savedOrder);
        productService.increaseStock(savedOrder.getProduct().getId(), savedOrder.getQuantity());
        throw new PaymentException("결제 요청 중 오류 발생: " + e.getMessage(), e);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(
          "Thread was interrupted while acquiring lock");
    } finally {
      if (lock.isLocked() && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> getUserOrders(
      OrderStatus status,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Pageable pageable) {
    String username = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
    User user = userRepository
        .findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

    Page<Order> orders;
    if (status != null && startDate != null && endDate != null) {
      orders = orderRepository.findByUserAndStatusAndOrderDateBetween(
          user,
          status,
          startDate,
          endDate,
          pageable);
    } else if (status != null) {
      orders = orderRepository.findByUserAndStatus(user, status, pageable);
    } else if (startDate != null && endDate != null) {
      orders = orderRepository.findByUserAndOrderDateBetween(
          user,
          startDate,
          endDate,
          pageable);
    } else {
      orders = orderRepository.findByUser(user, pageable);
    }

    return orders.map(order -> new OrderResponse(
        order.getId(),
        order.getUser().getUsername(),
        order.getProduct().getName(),
        order.getQuantity(),
        order.getTotalPrice(),
        order.getOrderDate(),
        order.getStatus(),
        null));
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderById(Long orderId) {
    String username = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
    User user = userRepository
        .findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

    Order order = orderRepository
        .findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

    if (!order.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException(
          "You are not authorized to view this order.");
    }

    return new OrderResponse(
        order.getId(),
        order.getUser().getUsername(),
        order.getProduct().getName(),
        order.getQuantity(),
        order.getTotalPrice(),
        order.getOrderDate(),
        order.getStatus(),
        null);
  }

  @Transactional
  public void updateOrderStatus(Long orderId, OrderStatus status) {
    Order order = orderRepository
        .findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    order.setStatus(status);
    orderRepository.save(order);
  }
}
