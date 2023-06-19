package com.somoff.shopproject.controllers;

import com.somoff.shopproject.entities.OrderStatus;
import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.services.CartService;
import com.somoff.shopproject.services.OrderService;
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
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/orders")
    public String getUserOrders(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("orders", orderService.getOrders(user));
        model.addAttribute("allPrice", cartService.getCartPrice(user));
        model.addAttribute("user", user);
        return "orders";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PostMapping(path = "/orders/{id}/edit/")
    public String updateOrderStatus(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long orderId,
                                    @RequestParam(name = "status") OrderStatus status) {
        orderService.updateOrder(user, orderId, status);
        return "redirect:/orders";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(path = "/orders/{id}/complete/")
    public String completeOrder(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long orderId) {
        orderService.updateOrder(user, orderId, OrderStatus.completed);
        return "redirect:/orders";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(path = "/orders/{id}/cancel/")
    public String cancelOrder(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long orderId) {
        orderService.updateOrder(user, orderId, OrderStatus.canceled);
        return "redirect:/orders";
    }
}
