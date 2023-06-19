package com.somoff.shopproject.rest;

import com.somoff.shopproject.entities.Product;

import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.repositories.ProductRepository;

import com.somoff.shopproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1")
public class ProductRestController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    @Autowired
    public ProductRestController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @GetMapping("/products/all")
    public List<Product> getRestProductList() {
      return productRepository.findAll();
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/products/active")
    public List<Product> getRestProductListByActive() {
        return productRepository.findAllByActive(true);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/all")
    public List<User> getRestUserList() {
        return userRepository.findAll().stream().peek(e->e.setPassword("****")).collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/my")
    public User getRestUser(@AuthenticationPrincipal User user) {
        User myUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        assert myUser != null;
        myUser.setPassword("****");
        return myUser;
    }
}
