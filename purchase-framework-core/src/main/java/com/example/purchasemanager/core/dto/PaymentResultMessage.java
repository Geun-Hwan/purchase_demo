package com.example.purchasemanager.core.dto;

import com.example.purchasemanager.core.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultMessage {
    private Long orderId;
    private OrderStatus status;
    private String message;
}
