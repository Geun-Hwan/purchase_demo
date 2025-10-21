package com.example.purchasedemo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.purchasedemo.dto.ProductRequest;
import com.example.purchasedemo.exception.InsufficientStockException;
import com.example.purchasedemo.exception.ProductNotFoundException;
import com.example.purchasedemo.repository.ProductRepository;
import com.example.purchasemanager.core.dto.ProductResponse;
import com.example.purchasemanager.core.entity.Product;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  @Transactional
  public ProductResponse createProduct(ProductRequest productRequest) {
    Product product = Product
        .builder()
        .name(productRequest.getName())
        .price(productRequest.getPrice())
        .stock(productRequest.getStock())
        .build();
    Product savedProduct = productRepository.save(product);
    return new ProductResponse(
        savedProduct.getId(),
        savedProduct.getName(),
        savedProduct.getPrice(),
        savedProduct.getStock());
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> getAllProducts() {
    return productRepository
        .findAll()
        .stream()
        .map(product -> new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock()))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ProductResponse getProductById(Long id) {
    Product product = productRepository
        .findById(id)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStock());
  }

  @Transactional(readOnly = true)
  public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
    return productRepository.findByNameContainingIgnoreCase(keyword, pageable)
        .map(
            product -> new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStock()));
  }

  @Transactional
  public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
    Product product = productRepository
        .findById(id)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    product.setName(productRequest.getName());
    product.setPrice(productRequest.getPrice());
    product.setStock(productRequest.getStock());
    Product updatedProduct = productRepository.save(product);
    return new ProductResponse(updatedProduct.getId(), updatedProduct.getName(), updatedProduct.getPrice(),
        updatedProduct.getStock());
  }

  @Transactional(readOnly = true)
  public Product getProductEntityById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
  }

  @Transactional
  public void decreaseStock(Long productId, Integer quantity) {
    try {
      Product product = getProductEntityById(productId);
      if (product.getStock() < quantity) {
        throw new InsufficientStockException(
            "Not enough stock for product: " + product.getName());
      }
      product.setStock(product.getStock() - quantity);
      productRepository.save(product);
    } catch (OptimisticLockException e) {
      throw new InsufficientStockException(
          "Failed to decrease stock due to concurrent modification. Please try again.");
    }
  }

  @Transactional
  public void increaseStock(Long productId, Integer quantity) {
    Product product = productRepository
        .findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
    product.setStock(product.getStock() + quantity);
    productRepository.save(product);
  }

  @Transactional
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new ProductNotFoundException("Product not found with id: " + id);
    }
    productRepository.deleteById(id);
  }
}
