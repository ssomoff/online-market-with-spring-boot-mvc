package com.somoff.shopproject.controllers;

import com.somoff.shopproject.entities.Product;
import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.services.CartService;
import com.somoff.shopproject.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;


    @Autowired
    public ProductController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/products")
    public String getProducts(@AuthenticationPrincipal User user, @RequestParam(required = false, defaultValue = "") String name,
                              @RequestParam(required = false, defaultValue = "1") Integer amount, Model model) {
        model.addAttribute("isEmptyProductList", productService.getProducts(name, user).isEmpty());
        model.addAttribute("products", productService.getProducts(name, user));
        model.addAttribute("productsAmount", productService.getProductsAmount());
        model.addAttribute("product", new Product());
        model.addAttribute("amount", amount);
        model.addAttribute("user", user);
        model.addAttribute("allPrice", cartService.getCartPrice(user));
        return "products";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PostMapping(path = "/products/add")
    public String addProduct(@ModelAttribute("product") Product product, @RequestParam("file") MultipartFile file,
                             @RequestParam Integer amount) throws IOException {
        productService.createProduct(product, amount, file);
        return "redirect:/products";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PostMapping(path = "/products/remove")
    public String removeProduct(@AuthenticationPrincipal User user, @RequestParam Long id) {
        productService.deactivateProduct(user, id);
        return "redirect:/products";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PostMapping(path = "/products/{id}/edit/")
    public String updateProduct(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long id,
                                @RequestParam("updName") String updName, @RequestParam("updPrice") Double updPrice,
                                @RequestParam("updDescription") String updDescription, @RequestParam("updWeight") Double updWeight,
                                @RequestParam("updPopularity") String updPopularity, @RequestParam(value = "updActive", defaultValue = "false") boolean updActive,
                                @RequestParam("updateFile") MultipartFile file,
                                @RequestParam("updAmount") Integer amount) throws IOException {


        productService.updateProduct(id, updName, updPrice, updDescription, updWeight, updPopularity, updActive, amount, file, user);
        return "redirect:/products";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PostMapping(path = "/products/{id}/remove/")
    public String deleteProduct(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long id) {
        productService.deactivateProduct(user, id);
        return "redirect:/products";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/products/{id}")
    public String getProduct(@AuthenticationPrincipal User user, @PathVariable(value = "id") Long id, Model model) {
        Product result = productService.findProductById(id, user);
        if (result == null) {
            model.addAttribute("message", "Product with id = "+id+" does not exist");
            return "error";
        }
        model.addAttribute("product", result);
        model.addAttribute("productAmount", productService.getProductAmountById(id));
        model.addAttribute("user", user);
        model.addAttribute("allPrice", cartService.getCartPrice(user));
        return "product";
    }
}
