package com.example.purchasedemo.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page; // Page import 추가
import org.springframework.data.domain.Pageable; // Pageable import 추가
import org.springframework.format.annotation.DateTimeFormat; // DateTimeFormat import 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.purchasedemo.dto.OrderRequest;
import com.example.purchasedemo.service.OrderService;
import com.example.purchasemanager.core.dto.ApiResponse;
import com.example.purchasemanager.core.dto.OrderResponse;
import com.example.purchasemanager.core.enums.OrderStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse createdOrder = orderService.createOrderAndInitiatePayment(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully!", createdOrder));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders( // List -> Page 변경
            @RequestParam(required = false) OrderStatus status, // OrderStatus 타입으로 변경
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable // Pageable 추가
    ) {
        Page<OrderResponse> orderResponses = orderService.getUserOrders(status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully!", orderResponses));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse orderResponse = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully!", orderResponse));
    }
}
