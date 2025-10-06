package com.example.purchasedemo.controller;

import com.example.purchasedemo.dto.TossPaymentResponse;
import com.example.purchasedemo.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final TossPaymentService tossPaymentService;

    @GetMapping("/success")
    public ResponseEntity<TossPaymentResponse> tossPaymentSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") Long amount) {
        TossPaymentResponse response = tossPaymentService.confirmPayment(paymentKey, orderId, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fail")
    public ResponseEntity<String> tossPaymentFail(
            @RequestParam("code") String code,
            @RequestParam("message") String message,
            @RequestParam("orderId") String orderId) {
        // TODO: 실패 로직 처리 및 실패 페이지로 리다이렉션 또는 JSON 응답 반환
        return ResponseEntity.badRequest().body("Payment failed: " + message);
    }
}
