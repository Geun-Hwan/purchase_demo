package com.example.purchasedemo.controller;

import com.example.purchasedemo.dto.ApiResponse;
import com.example.purchasedemo.dto.OrderRequest;
import com.example.purchasedemo.dto.OrderResponse;
import com.example.purchasedemo.entity.OrderStatus; // OrderStatus import 추가
import com.example.purchasedemo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Page import 추가
import org.springframework.data.domain.Pageable; // Pageable import 추가
import org.springframework.format.annotation.DateTimeFormat; // DateTimeFormat import 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse createdOrder = orderService.createOrderAndInitiatePayment(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order created successfully!", createdOrder));
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
