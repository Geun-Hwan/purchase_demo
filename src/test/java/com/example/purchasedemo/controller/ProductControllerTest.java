package com.example.purchasedemo.controller;

import com.example.purchasedemo.dto.ProductRequest;
import com.example.purchasedemo.entity.Product;
import com.example.purchasedemo.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        savedProduct = productRepository.save(Product.builder()
                .name("Initial Product")
                .price(100.0)
                .stock(10)
                .build());
    }

    @Test
    @DisplayName("관리자 권한으로 상품 생성 성공")
    @WithMockUser(roles = "ADMIN")
    void createProduct_success() throws Exception {
        // given
        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setPrice(200.0);
        request.setStock(20);

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("New Product"));
    }

    @Test
    @DisplayName("일반 유저 권한으로 상품 생성 실패 (403 Forbidden)")
    @WithMockUser(roles = "USER")
    void createProduct_fail_byNormalUser() throws Exception {
        // given
        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setPrice(200.0);
        request.setStock(20);

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상품 ID로 조회 성공")
    @WithMockUser
    void getProductById_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(savedProduct.getName()));
    }

    @Test
    @DisplayName("관리자 권한으로 상품 수정 성공")
    @WithMockUser(roles = "ADMIN")
    void updateProduct_success() throws Exception {
        // given
        ProductRequest request = new ProductRequest();
        request.setName("Updated Product");
        request.setPrice(150.0);
        request.setStock(5);

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Product"));
    }

    @Test
    @DisplayName("관리자 권한으로 상품 삭제 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
