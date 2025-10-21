package com.example.purchasemanager.payment.toss.dto; // Changed package

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentRequest {
    private String method; // 결제 수단
    private Long amount; // 결제 금액
    private String orderId; // 주문 ID
    private String orderName; // 주문명
    private String successUrl; // 성공 시 리다이렉트 URL
    private String failUrl; // 실패 시 리다이렉트 URL
}
