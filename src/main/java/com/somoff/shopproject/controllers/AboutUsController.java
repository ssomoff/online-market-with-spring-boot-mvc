package com.somoff.shopproject.controllers;

import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutUsController {

    private final CartService cartService;

    @Autowired
    public AboutUsController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping(path = "/about")
    public String getAbout(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("allPrice", cartService.getCartPrice(user));
        return "about";
    }
}

