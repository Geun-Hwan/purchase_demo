package com.example.purchasedemo.dto;

import com.example.purchasedemo.entity.OrderStatus; // OrderStatus import 추가
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultMessage {
    private Long orderId;
    private OrderStatus status; // String -> OrderStatus 변경
    private String message;
}
