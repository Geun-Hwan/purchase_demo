package com.example.purchasedemo.util;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.purchasedemo.repository.ProductRepository;
import com.example.purchasedemo.repository.UserRepository;
import com.example.purchasemanager.core.entity.Product;
import com.example.purchasemanager.core.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("local")
public class DataLoader implements CommandLineRunner {

  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    if (userRepository.findByUsername("admin").isEmpty()) {
      User adminUser = User
          .builder()
          .username("admin")
          .password(passwordEncoder.encode("adminpassword"))
          .roles(Collections.singleton("ROLE_ADMIN"))
          .build();
      userRepository.save(adminUser);
      log.info("Admin user created: admin/adminpassword");
    }

    if (productRepository.count() == 0) {
      Product product1 = Product
          .builder()
          .name("Laptop")
          .price(1200.00)
          .stock(10)
          .build();
      Product product2 = Product
          .builder()
          .name("Mouse")
          .price(2400.00)
          .stock(50)
          .build();
      Product product3 = Product
          .builder()
          .name("Keyboard")
          .price(7500.00)
          .stock(30)
          .build();

      productRepository.saveAll(Arrays.asList(product1, product2, product3));
      log.info("Initial products created.");
    }
  }
}
