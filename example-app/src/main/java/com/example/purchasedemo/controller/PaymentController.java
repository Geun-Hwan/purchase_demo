package com.example.purchasedemo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.purchasedemo.service.PaymentProcessor;
import com.example.purchasemanager.core.dto.ApiResponse;
import com.example.purchasemanager.core.enums.PaymentGatewayType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessor paymentProcessor;

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<Map<String, Long>>> paymentSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") Long amount) {
        try {
            Long processedOrderId = paymentProcessor.processPaymentConfirmation(PaymentGatewayType.TOSS, paymentKey, orderId, amount);
            return ResponseEntity.ok(ApiResponse.success("결제 처리가 성공적으로 시작되었습니다.", Map.of("orderId", processedOrderId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("결제 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/fail")
    public ResponseEntity<ApiResponse<String>> paymentFail(
            @RequestParam("code") String code,
            @RequestParam("message") String message,
            @RequestParam("orderId") String orderId) {

        paymentProcessor.processPaymentFailure(PaymentGatewayType.TOSS, orderId, message);

        return ResponseEntity.badRequest().body(ApiResponse.error("결제에 실패했습니다: " + message));
    }
}
