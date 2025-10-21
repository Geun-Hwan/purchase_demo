package com.example.purchasedemo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.purchasedemo.dto.ProductRequest;
import com.example.purchasedemo.exception.InsufficientStockException;
import com.example.purchasedemo.exception.ProductNotFoundException;
import com.example.purchasedemo.repository.ProductRepository;
import com.example.purchasemanager.core.dto.ProductResponse;
import com.example.purchasemanager.core.entity.Product;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품 생성 성공")
    void createProduct_success() {
        // given
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setPrice(100.0);
        request.setStock(10);

        Product savedProduct = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // when
        ProductResponse response = productService.createProduct(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
    }

    @Test
    @DisplayName("ID로 상품 조회 성공")
    void getProductById_success() {
        // given
        Product product = new Product("Test Product", 100.0, 10);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // when
        ProductResponse response = productService.getProductById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 상품 조회 시 예외 발생")
    void getProductById_fail_whenProductNotFound() {
        // given
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProductById(1L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("재고 감소 성공")
    void decreaseStock_success() {
        // given
        Product product = new Product("Test Product", 100.0, 10);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // when
        productService.decreaseStock(1L, 5);

        // then
        assertThat(product.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("재고보다 많은 양 감소 시 예외 발생")
    void decreaseStock_fail_whenStockNotEnough() {
        // given
        Product product = new Product("Test Product", 100.0, 10);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.decreaseStock(1L, 15))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    @DisplayName("재고 증가 성공")
    void increaseStock_success() {
        // given
        Product product = new Product("Test Product", 100.0, 10);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // when
        productService.increaseStock(1L, 5);

        // then
        assertThat(product.getStock()).isEqualTo(15);
    }
}
