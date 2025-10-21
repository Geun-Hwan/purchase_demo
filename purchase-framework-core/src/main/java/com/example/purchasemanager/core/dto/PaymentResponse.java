package com.example.purchasemanager.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String paymentId; // 결제 고유 ID (예: Toss paymentKey)
    private String status; // 결제 상태 (예: READY, SUCCESS, FAILED)
    private String checkoutUrl; // 결제 진행을 위한 URL (필요한 경우)
    private String orderId; // 주문 ID
    private Long amount; // 결제 금액
    private String message; // 추가 메시지
}
