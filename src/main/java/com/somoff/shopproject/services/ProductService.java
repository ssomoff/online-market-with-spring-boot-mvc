package com.somoff.shopproject.services;

import com.somoff.shopproject.entities.*;
import com.somoff.shopproject.repositories.ProductAmountRepository;
import com.somoff.shopproject.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ProductService {

    @Value("${upload.path}")
    private String uploadPath;

    private final ProductRepository productRepository;
    private final ProductAmountRepository productAmountRepository;


    @Autowired
    public ProductService(ProductRepository productRepository, ProductAmountRepository productAmountRepository) {
        this.productRepository = productRepository;
        this.productAmountRepository = productAmountRepository;
    }

    @Transactional
    public void createProduct(Product product, Integer amount, MultipartFile file) throws IOException {
        createDir();
        if (file != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
            String uuidFile = UUID.randomUUID().toString();
            String pictureName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + pictureName));
            product.setPictureName(pictureName);
        } else {
            product.setPictureName("default-product-image.png");
        }
        addProduct(product);
        createProductAmount(product, amount);
    }

    @Transactional
    public void updateProduct(Long id, String updName, Double updPrice, String updDescription, Double updWeight,
                              String updPopularity, boolean updActive, Integer amount, MultipartFile file, User user) throws IOException {
        Product updProduct = findProductById(id, user);
        updProduct.setName(updName);
        updProduct.setPrice(updPrice);
        updProduct.setDescription(updDescription);
        updProduct.setWeight(updWeight);
        updProduct.setPopularity(updPopularity);
        updProduct.setActive(updActive);
        if (file != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
            String uuidFile = UUID.randomUUID().toString();
            String pictureName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + pictureName));
            updProduct.setPictureName(pictureName);
        }
        addProduct(updProduct);
        updateProductAmount(updProduct, amount);
    }

    @Transactional
    public void deactivateProduct(User user, Long id) {
        Product updProduct = findProductById(id, user);
        updProduct.setActive(false);
        addProduct(updProduct);
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(String name, User user) {
        if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.MANAGER)) {
            if (name != null && !name.isEmpty()) {
                return productRepository.findProductsByName(name);
            } else {
                return productRepository.findAll();
            }
        } else if (name != null && !name.isEmpty()) {
            return productRepository.findProductsByNameAndActive(name);
        } else {
            return productRepository.findAllByActive(true);
        }
    }

    @Transactional(readOnly = true)
    public List<Product> findPopProducts() {
        return productRepository.findProductsByPopularityAndActive("true");
    }

    @Transactional(readOnly = true)
    public List<ProductAmount> getProductsAmount() {
        return productAmountRepository.findAll();
    }

    public Product findProductById(Long id, User user) {
        if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.MANAGER)) {
            return productRepository.findById(id).orElse(null);
        }
        if (user.getRoles().contains(Role.USER)) {
            return productRepository.findProductByIdAndActive(id).orElse(null);
        }
        return null;
    }

    public Product findProductByIdForReturnInStack(Long id, User user) {
        if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.MANAGER) || user.getRoles().contains(Role.USER)) {
            return productRepository.findById(id).orElse(null);
        }
        return null;
    }

    public ProductAmount getProductAmountById(Long id) {
        return productAmountRepository.findById(id).orElse(null);
    }

    public void updateProductAmount(Product product, Integer amount) {
        ProductAmount productAmount = productAmountRepository.findById(product.getId()).orElse(null);
        assert productAmount != null;
        productAmount.setProduct(product);
        productAmount.setAmount(amount);
        productAmountRepository.save(productAmount);
        if (amount == 0) {
            product.setActive(false);
            addProduct(product);
        }
    }

    private void addProduct(Product product) {
        productRepository.save(product);
    }

    private void createDir() {
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
    }

    private void createProductAmount(Product product, Integer amount) {
        ProductAmount productAmount = new ProductAmount();
        productAmount.setProduct(product);
        productAmount.setAmount(amount);
        productAmountRepository.save(productAmount);
    }
}
