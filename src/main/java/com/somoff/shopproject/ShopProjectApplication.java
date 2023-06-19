package com.somoff.shopproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.somoff.shopproject")
public class ShopProjectApplication {

    public static void main(String[] args) {

        SpringApplication.run(ShopProjectApplication.class, args);

    }

}
