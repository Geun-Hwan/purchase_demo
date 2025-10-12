package com.example.purchasedemo.service.payment;

import com.example.purchasedemo.dto.TossPaymentResponse;
import com.example.purchasedemo.entity.Order;

// 모든 결제 시스템이 따라야 하는 규칙을 정의하는 인터페이스
public interface PaymentGateway {

    /**
     * 결제 생성을 요청합니다.
     * @param order 주문 정보
     * @return 결제 페이지 URL 등이 담긴 응답 객체
     */
    TossPaymentResponse requestPayment(Order order);

    /**
     * 최종 결제를 승인합니다.
     * @param paymentKey 결제사에서 발급한 키
     * @param orderId 주문 ID
     * @param amount 금액
     * @return 결제 승인 결과 정보
     */
    TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount);

    // TODO: 향후 결제 취소(환불) 메소드 등 추가
}
