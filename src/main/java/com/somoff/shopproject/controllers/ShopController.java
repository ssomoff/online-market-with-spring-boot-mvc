package com.somoff.shopproject.controllers;

import com.somoff.shopproject.entities.Product;
import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.services.CartService;
import com.somoff.shopproject.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
public class ShopController {

    private final ProductService productService;

    private final CartService cartService;

    @Autowired
    public ShopController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }


    @GetMapping(path = "/")
    public String getHomePage(@AuthenticationPrincipal User user, Model model) {
        List<Product> products = productService.findPopProducts();
        model.addAttribute("productsAmount", productService.getProductsAmount());
        model.addAttribute("isEmptyProductList", products.isEmpty());
        model.addAttribute("products", products);
        model.addAttribute("user", user);
        model.addAttribute("allPrice", cartService.getCartPrice(user));
        return "home";
    }

}
