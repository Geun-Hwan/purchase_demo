package com.example.purchasedemo.controller;

import com.example.purchasedemo.dto.ApiResponse;
import com.example.purchasedemo.service.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessor paymentProcessor;

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<String>> tossPaymentSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") Long amount) {
        try {
            paymentProcessor.processTossPaymentConfirmation(paymentKey, orderId, amount);
            // 성공 시, 클라이언트에게 간단한 성공 메시지 또는 리다이렉션 주소를 반환할 수 있습니다.
            return ResponseEntity.ok(ApiResponse.success("결제 처리가 성공적으로 시작되었습니다."));
        } catch (Exception e) {
            // 실패 시, 에러 메시지를 반환합니다.
            return ResponseEntity.badRequest().body(ApiResponse.error("결제 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/fail")
    public ResponseEntity<ApiResponse<String>> tossPaymentFail(
            @RequestParam("code") String code,
            @RequestParam("message") String message,
            @RequestParam("orderId") String orderId) {
        // 실패 로직은 PaymentProcessor에서 이미 처리되고 Kafka 메시지가 발행됩니다.
        // 여기서는 클라이언트에게 실패를 알리는 응답만 반환합니다.
        return ResponseEntity.badRequest().body(ApiResponse.error("결제에 실패했습니다: " + message));
    }
}
