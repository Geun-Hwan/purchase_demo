package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.OrderRequest;
import com.example.purchasedemo.dto.OrderResponse;
import com.example.purchasedemo.dto.TossPaymentResponse;
import com.example.purchasedemo.entity.Order;
import com.example.purchasedemo.entity.Product;
import com.example.purchasedemo.entity.User;
import com.example.purchasedemo.repository.OrderRepository;
import com.example.purchasedemo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductService productService;
  private final TossPaymentService tossPaymentService; // TossPaymentService 주입
  private final RedissonClient redissonClient;

  @Transactional
  public OrderResponse createOrder(OrderRequest orderRequest) {
    String username = SecurityContextHolder.getContext()
      .getAuthentication()
      .getName();
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() ->
        new IllegalArgumentException("User not found: " + username)
      );

    RLock lock = redissonClient.getLock(
      "product_lock:" + orderRequest.getProductId()
    );
    try {
      boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
      if (!isLocked) {
        throw new IllegalStateException(
          "Could not acquire lock for product " +
          orderRequest.getProductId()
        );
      }

      Product product = productService.getProductEntityById(
        orderRequest.getProductId()
      );

      productService.decreaseStock(
        product.getId(),
        orderRequest.getQuantity()
      );

      Double totalPrice = product.getPrice() * orderRequest.getQuantity();

      Order order = Order
        .builder()
        .user(user)
        .product(product)
        .quantity(orderRequest.getQuantity())
        .totalPrice(totalPrice)
        .orderDate(LocalDateTime.now())
        .status("PENDING") // Explicitly set status to PENDING
        .build();

      Order savedOrder = orderRepository.save(order);

      // Toss Payments에 결제 생성 요청
      TossPaymentResponse tossPaymentResponse = tossPaymentService.requestPayment(savedOrder);
      String paymentUrl = tossPaymentResponse.getCheckoutUrl();

      return new OrderResponse(
        savedOrder.getId(),
        savedOrder.getUser().getUsername(),
        savedOrder.getProduct().getName(),
        savedOrder.getQuantity(),
        savedOrder.getTotalPrice(),
        savedOrder.getOrderDate(),
        paymentUrl // paymentUrl 추가
      );
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(
        "Thread was interrupted while acquiring lock"
      );
    } finally {
      if (lock.isLocked() && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> getUserOrders() {
    String username = SecurityContextHolder.getContext()
      .getAuthentication()
      .getName();
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() ->
        new IllegalArgumentException("User not found: " + username)
      );

    List<Order> orders = orderRepository.findByUser(user).orElse(List.of());

    return orders
      .stream()
      .map(order ->
        new OrderResponse(
          order.getId(),
          order.getUser().getUsername(),
          order.getProduct().getName(),
          order.getQuantity(),
          order.getTotalPrice(),
          order.getOrderDate()
        )
      )
      .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderById(Long orderId) {
    String username = SecurityContextHolder.getContext()
      .getAuthentication()
      .getName();
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() ->
        new IllegalArgumentException("User not found: " + username)
      );

    Order order = orderRepository
      .findById(orderId)
      .orElseThrow(() ->
        new IllegalArgumentException("Order not found with id: " + orderId)
      );

    if (!order.getUser().getId().equals(user.getId())) {
      throw new IllegalArgumentException(
        "You are not authorized to view this order."
      );
    }

    return new OrderResponse(
      order.getId(),
      order.getUser().getUsername(),
      order.getProduct().getName(),
      order.getQuantity(),
      order.getTotalPrice(),
      order.getOrderDate()
    );
  }

  @Transactional
  public void updateOrderStatus(Long orderId, String status) {
    Order order = orderRepository
      .findById(orderId)
      .orElseThrow(() ->
        new IllegalArgumentException("Order not found with id: " + orderId)
      );
    order.setStatus(status);
    orderRepository.save(order);
  }
}
