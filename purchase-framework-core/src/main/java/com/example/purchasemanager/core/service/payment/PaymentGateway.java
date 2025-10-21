package com.example.purchasemanager.core.service.payment;

import com.example.purchasemanager.core.dto.PaymentResponse;
import com.example.purchasemanager.core.entity.Order;

public interface PaymentGateway {

    PaymentResponse requestPayment(Order order);

    PaymentResponse confirmPayment(String paymentKey, String orderId, Long amount);
}
