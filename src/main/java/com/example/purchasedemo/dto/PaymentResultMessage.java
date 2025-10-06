package com.example.purchasedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultMessage {
    private Long orderId;
    private String status; // e.g., "SUCCESS", "FAILED"
    private String message;
}
