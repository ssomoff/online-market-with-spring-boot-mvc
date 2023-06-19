package com.somoff.shopproject.controllers;

import com.somoff.shopproject.entities.Role;
import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.services.CartService;
import com.somoff.shopproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class AccountController {

    private final UserService userService;
    private final CartService cartService;


    @Autowired
    public AccountController(UserService userService, CartService cartService) {
        this.userService = userService;
        this.cartService = cartService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(path = "/users")
    public String getUsers(@AuthenticationPrincipal User user, Model model) {
        List<User> users = userService.getUsers();
        model.addAttribute("users", users);
        model.addAttribute("user", user);
        return "users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = "/users/{id}/role")
    public String updateUserRole(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long userId,
                                 @RequestParam(name = "role") Role role) {
        userService.updateUserRole(user, userId, role);
        return "redirect:/users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = "/users/{id}/deactivate")
    public String deactivateUser(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long userId) {
        userService.activateAccount(user, userId, false);
        return "redirect:/users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = "/users/{id}/activate")
    public String activateUser(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long userId) {
        userService.activateAccount(user, userId, true);
        return "redirect:/users";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/profile")
    public String getProfile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("userForm", user);
        model.addAttribute("allPrice", cartService.getCartPrice(user));
        return "profile";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/profile")
    public String updateProfile(@AuthenticationPrincipal User user, @ModelAttribute("userForm") User userForm,
                                @RequestParam String password, @RequestParam("file") MultipartFile file, Model model) throws IOException {
        int i = userService.updateUser(user, userForm, password, file);

        if (i == 0) {
            model.addAttribute("user", user);
            model.addAttribute("userForm", userForm);
            model.addAttribute("allPrice", cartService.getCartPrice(user));
            model.addAttribute("messageErr", "User with this email exists");
            return "profile";
        }
        if (i == 2) {
            model.addAttribute("user", user);
            model.addAttribute("userForm", userForm);
            model.addAttribute("allPrice", cartService.getCartPrice(user));
            model.addAttribute("messageErr", "Password must match");
            return "profile";
        }
        if (i == 3) {
            model.addAttribute("message", "Updated successfully");
            model.addAttribute("messageArr", "A letter with instruction to activate your account has been sent to your email");
        }else {
            model.addAttribute("user", user);
            model.addAttribute("userForm", userForm);
            model.addAttribute("message", "Updated successfully");
        }
        return "reAuthorized";
    }

}


