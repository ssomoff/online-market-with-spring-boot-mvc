package com.somoff.shopproject.repositories;

import com.somoff.shopproject.entities.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

}
