package com.example.purchasedemo.dto;

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
    private String paymentUrl; // 결제 URL 필드 추가

    // paymentUrl을 제외한 필드를 포함하는 생성자 (기존 호환성 유지)
    public OrderResponse(Long orderId, String username, String productName, Integer quantity, Double totalPrice,
            LocalDateTime orderDate) {
        this.orderId = orderId;
        this.username = username;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }
}
