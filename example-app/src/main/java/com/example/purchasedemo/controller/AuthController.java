package com.example.purchasedemo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.purchasedemo.dto.LoginRequest;
import com.example.purchasedemo.dto.SignupRequest;
import com.example.purchasedemo.service.AuthService;
import com.example.purchasemanager.core.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> registerUser(@RequestBody SignupRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Map<String, String> tokenMap = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful!", tokenMap));
    }
}
