package com.somoff.shopproject.controllers;

import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/registration")
    public String getRegistration(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }


    @PostMapping(path = "/registration")
    public String executeRegistration(@ModelAttribute("user") User user, @RequestParam String address2,
                                      @RequestParam String rePassword, @RequestParam("file") MultipartFile file, Model model) throws IOException {
        int i = userService.createUser(user, address2, rePassword, file);
        if (i == 1) {
            model.addAttribute("messageArr", "A letter with instruction to activate your account has been sent to your email");
            return "login";
        }
        if (i == 0) {
            model.addAttribute("message", "User with this email exists");
        } else {
            model.addAttribute("message", "Passwords must match");
        }
        return "registration";

    }

    @GetMapping(path = "/registration/admin")
    public String createAdmin(Model model) {
        boolean isAdmin = userService.createAdmin();
        if (isAdmin) {
            model.addAttribute("message", "User with this email exists");
            return "registration";
        } else
            return "redirect:/login";
    }

    @GetMapping(path = "/activate/{code}")
    public String activateAccount(@PathVariable String code, Model model) {
        boolean isActivated = userService.activateAccount(code);
        if (isActivated) {
            model.addAttribute("message", "Your account successfully activated");
        } else {
            model.addAttribute("messageErr", "Activation code is not found");
        }
        return "login";
    }
}
