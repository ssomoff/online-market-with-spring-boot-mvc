package com.somoff.shopproject.controllers;

import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(path = "/cart")
    public String getUserCart(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("ifCartProductsNull", cartService.getCartProductList(user).iterator().hasNext());
        model.addAttribute("cartProducts", cartService.getCartProductList(user));
        model.addAttribute("allPrice", cartService.getCartPrice(user));
        model.addAttribute("dates", cartService.getDate());
        model.addAttribute("user", user);
        return "cart";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(path = "/cart/confirm")
    public String confirmUserOrders(@AuthenticationPrincipal User user) {
        cartService.createOrder(user);
        return "redirect:/orders";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(path = "/cart/clear")
    public String getClearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return "redirect:/cart";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(path = "/cart/{id}/remove/")
    public String deleteProductInCart(@AuthenticationPrincipal User user,
                                      @PathVariable(value = "id") Long productId) {
        cartService.deleteCartProduct(user, productId);
        return "redirect:/cart";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(path = "/cart/{id}/add/")
    public String addProductInCart(@AuthenticationPrincipal User user, @RequestParam Integer amount,
                                   @PathVariable(value = "id") Long productId) {
        cartService.addCartProduct(user, amount, productId);
        return "redirect:/products";
    }
}
