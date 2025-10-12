package com.example.purchasedemo.dto;

import com.example.purchasedemo.entity.OrderStatus; // OrderStatus import 추가
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String username;
    private String productName;
    private Integer quantity;
    private Double totalPrice;
    private LocalDateTime orderDate;
    private OrderStatus status; // OrderStatus 필드 추가
    private String paymentUrl; // 결제 URL 필드
}
