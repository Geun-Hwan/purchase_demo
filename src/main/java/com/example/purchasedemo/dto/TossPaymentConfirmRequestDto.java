package com.example.purchasedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentConfirmRequestDto {
    private String orderId;
    private Long amount;
}
