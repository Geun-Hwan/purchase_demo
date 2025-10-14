package com.example.purchasedemo.controller;

import com.example.purchasedemo.dto.OrderRequest;
import com.example.purchasedemo.dto.TossPaymentResponse;
import com.example.purchasedemo.entity.Product;
import com.example.purchasedemo.entity.User;
import com.example.purchasedemo.repository.ProductRepository;
import com.example.purchasedemo.repository.UserRepository;
import com.example.purchasedemo.service.payment.PaymentGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PaymentGateway paymentGateway;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        savedProduct = productRepository.save(Product.builder()
                .name("Test Product")
                .price(100.0)
                .stock(10)
                .build());

        User testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of("ROLE_USER"))
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("사용자 권한으로 주문 생성 성공")
    @WithMockUser(username = "testuser", roles = "USER")
    void createOrder_success() throws Exception {
        // given
        OrderRequest request = new OrderRequest(savedProduct.getId(), 2);

        TossPaymentResponse tossResponse = new TossPaymentResponse();
        TossPaymentResponse.Checkout checkout = new TossPaymentResponse.Checkout();
        checkout.setUrl("https://payment.url");
        tossResponse.setCheckout(checkout);
        tossResponse.setStatus("READY");

        when(paymentGateway.requestPayment(any())).thenReturn(tossResponse);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Test Product"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andExpect(jsonPath("$.data.paymentUrl").value("https://payment.url"));
    }
}
