package com.example.purchasedemo; // Changed package

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan; // Added import
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.example.purchasedemo", "com.example.purchasemanager"})
@EnableScheduling
@EntityScan(basePackages = { "com.example.purchasemanager.core.entity" }) // Added EntityScan
public class PurchaseDemoApplication { // Changed class name

    public static void main(String[] args) {
        SpringApplication.run(PurchaseDemoApplication.class, args);
    }

}
